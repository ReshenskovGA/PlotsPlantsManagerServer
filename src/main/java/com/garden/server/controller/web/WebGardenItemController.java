package com.garden.server.controller.web;

import com.garden.server.dto.BedDto;
import com.garden.server.dto.PlotDto;
import com.garden.server.dto.TaskDto;
import com.garden.server.dto.TreebushDto;
import com.garden.server.entity.Plant;
import com.garden.server.entity.PlantConst;
import com.garden.server.entity.User;
import com.garden.server.repository.PlantConstRepository;
import com.garden.server.repository.PlantRepository;
import com.garden.server.repository.UserRepository;
import com.garden.server.service.BedService;
import com.garden.server.service.PlotService;
import com.garden.server.service.TaskService;
import com.garden.server.service.TreebushService;
import com.garden.server.utils.RecurrenceUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/web/items")
@RequiredArgsConstructor
public class WebGardenItemController {
    private final BedService bedService;
    private final TreebushService treebushService;
    private final PlotService plotService;
    private final UserRepository userRepository;
    private final PlantRepository plantRepository;
    private final PlantConstRepository plantConstRepository;
    private final TaskService taskService;
    private final RecurrenceUtils recurrenceUtils;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    private Long getCurrentUserId(Authentication authentication) {
        String login = authentication.getName();
        return userRepository.findByLogin(login)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден в сессии"));
    }

    private String getPlantName(Long plantId) {
        if (plantId == null) return "Не посажено";
        if (plantId < 0) {
            return plantConstRepository.findById(Math.abs(plantId)).map(PlantConst::getName).orElse("Растение");
        } else {
            return plantRepository.findById(plantId).map(Plant::getName).orElse("Растение");
        }
    }

    private List<PlantOption> getPlantOptions(Long userId) {
        List<PlantOption> options = new ArrayList<>();
        options.add(new PlantOption(0L, "Не посажено"));
        plantConstRepository.findAll().forEach(p ->
                options.add(new PlantOption(-p.getId(), p.getName() + " (Справочник)"))
        );
        plantRepository.findByUserId(userId).forEach(p ->
                options.add(new PlantOption(p.getId(), p.getName() + " (Личное)"))
        );
        return options;
    }

    private String getBackUrl(String from, Long plotId) {
        if ("list".equals(from)) {
            return contextPath + "/web/items";
        }
        return contextPath + "/web/plots/" + plotId;
    }

    private String getPlantViewUrl(Long plantId) {
        if (plantId == null) return null;
        if (plantId < 0) {
            return contextPath + "/web/plantedplants/view/" + Math.abs(plantId) + "?source=catalog";
        } else {
            return contextPath + "/web/plantedplants/view/" + plantId + "?source=personal";
        }
    }

    private String intToHexColor(Integer color) {
        if (color == null) return "#9E9E9E";
        return String.format("#%06X", (0xFFFFFF & color));
    }

    @Data
    @AllArgsConstructor
    public static class PlantOption {
        private Long id;
        private String name;
    }


    @GetMapping("/bed/add")
    public String showAddBedForm(@RequestParam Long plotId,
                                 @RequestParam(required = false, defaultValue = "plot") String from,
                                 Authentication auth, Model model) {
        Long userId = getCurrentUserId(auth);
        model.addAttribute("item", new BedDto.Request());
        model.addAttribute("type", "bed");
        model.addAttribute("plots", plotService.getPlotsByUser(userId));
        model.addAttribute("selectedPlotId", plotId);
        model.addAttribute("plantOptions", getPlantOptions(userId));
        model.addAttribute("from", from);
        model.addAttribute("backUrl", getBackUrl(from, plotId));
        return "garden-item-form";
    }

    @GetMapping("/bed/edit/{id}")
    public String showEditBedForm(@PathVariable Long id,
                                  @RequestParam(required = false, defaultValue = "plot") String from,
                                  Authentication auth, Model model) {
        Long userId = getCurrentUserId(auth);
        BedDto.Response bed = bedService.getBedById(id, userId);
        model.addAttribute("item", bed);
        model.addAttribute("type", "bed");
        model.addAttribute("plots", plotService.getPlotsByUser(userId));
        model.addAttribute("plantOptions", getPlantOptions(userId));
        model.addAttribute("from", from);
        model.addAttribute("backUrl", getBackUrl(from, bed.getPlotId()));
        return "garden-item-form";
    }

    @PostMapping("/bed/save")
    public String saveBed(@ModelAttribute BedDto.Request request, Authentication auth) {
        Long userId = getCurrentUserId(auth);
        if (request.getPlantId() != null && request.getPlantId() == 0L) {
            request.setPlantId(null);
        }
        if (request.getId() != null) {
            bedService.updateBed(request.getId(), request, userId);
        } else {
            bedService.createBed(request, userId);
        }
        return "redirect:" + contextPath + "/web/plots/" + request.getPlotId();
    }

    @PostMapping("/bed/delete/{id}")
    public String deleteBed(@PathVariable Long id, Authentication auth) {
        Long userId = getCurrentUserId(auth);
        BedDto.Response bed = bedService.getBedById(id, userId);
        Long plotId = bed.getPlotId();
        bedService.deleteBed(id, userId);
        return "redirect:" + contextPath + "/web/plots/" + plotId;
    }

    @GetMapping("/bed/view/{id}")
    public String viewBed(@PathVariable Long id,
                          @RequestParam(required = false, defaultValue = "plot") String from,
                          Authentication auth, Model model) {
        Long userId = getCurrentUserId(auth);
        BedDto.Response bed = bedService.getBedById(id, userId);
        PlotDto.Response plot = plotService.getPlotById(bed.getPlotId(), userId);
        model.addAttribute("item", bed);
        model.addAttribute("plot", plot);
        model.addAttribute("plantName", getPlantName(bed.getPlantId()));
        model.addAttribute("itemType", "Грядка");
        model.addAttribute("colorHex", intToHexColor(bed.getMarkerColor()));
        model.addAttribute("from", from);
        model.addAttribute("backUrl", getBackUrl(from, bed.getPlotId()));

        model.addAttribute("plantViewUrl", getPlantViewUrl(bed.getPlantId()));

        List<TaskDto.Response> allTasks = taskService.getTasksByUser(userId);
        String targetType = "BED";

        List<TaskDto.Response> itemTasks = allTasks.stream()
                .filter(t -> targetType.equals(t.getPlantedItemType()) && id.equals(t.getPlantedItemId()))
                .collect(java.util.stream.Collectors.toList());

        LocalDate now = LocalDate.now();
        List<TaskDto.Response> expandedItemTasks = recurrenceUtils.expandTasks(itemTasks, now.minusMonths(1), now.plusYears(1));

        expandedItemTasks.sort(java.util.Comparator.comparing(TaskDto.Response::getDate));
        model.addAttribute("itemTasks", expandedItemTasks);
        return "garden-item-view";
    }


    @GetMapping("/treebush/add")
    public String showAddTreebushForm(@RequestParam Long plotId,
                                      @RequestParam(required = false, defaultValue = "plot") String from,
                                      Authentication auth, Model model) {
        Long userId = getCurrentUserId(auth);
        model.addAttribute("item", new TreebushDto.Request());
        model.addAttribute("type", "treebush");
        model.addAttribute("plots", plotService.getPlotsByUser(userId));
        model.addAttribute("selectedPlotId", plotId);
        model.addAttribute("plantOptions", getPlantOptions(userId));
        model.addAttribute("from", from);
        model.addAttribute("backUrl", getBackUrl(from, plotId));
        return "garden-item-form";
    }

    @GetMapping("/treebush/edit/{id}")
    public String showEditTreebushForm(@PathVariable Long id,
                                       @RequestParam(required = false, defaultValue = "plot") String from,
                                       Authentication auth, Model model) {
        Long userId = getCurrentUserId(auth);
        TreebushDto.Response treebush = treebushService.getTreebushById(id, userId);
        model.addAttribute("item", treebush);
        model.addAttribute("type", "treebush");
        model.addAttribute("plots", plotService.getPlotsByUser(userId));
        model.addAttribute("plantOptions", getPlantOptions(userId));
        model.addAttribute("from", from);
        model.addAttribute("backUrl", getBackUrl(from, treebush.getPlotId()));
        return "garden-item-form";
    }

    @PostMapping("/treebush/save")
    public String saveTreebush(@ModelAttribute TreebushDto.Request request, Authentication auth) {
        Long userId = getCurrentUserId(auth);
        if (request.getPlantId() != null && request.getPlantId() == 0L) {
            request.setPlantId(null);
        }
        if (request.getId() != null) {
            treebushService.updateTreebush(request.getId(), request, userId);
        } else {
            treebushService.createTreebush(request, userId);
        }
        return "redirect:" + contextPath + "/web/plots/" + request.getPlotId();
    }

    @PostMapping("/treebush/delete/{id}")
    public String deleteTreebush(@PathVariable Long id, Authentication auth) {
        Long userId = getCurrentUserId(auth);
        TreebushDto.Response tree = treebushService.getTreebushById(id, userId);
        Long plotId = tree.getPlotId();
        treebushService.deleteTreebush(id, userId);
        return "redirect:" + contextPath + "/web/plots/" + plotId;
    }

    @GetMapping("/treebush/view/{id}")
    public String viewTreebush(@PathVariable Long id,
                               @RequestParam(required = false, defaultValue = "plot") String from,
                               Authentication auth, Model model) {
        Long userId = getCurrentUserId(auth);
        TreebushDto.Response tree = treebushService.getTreebushById(id, userId);
        PlotDto.Response plot = plotService.getPlotById(tree.getPlotId(), userId);
        model.addAttribute("item", tree);
        model.addAttribute("plot", plot);
        model.addAttribute("plantName", getPlantName(tree.getPlantId()));
        model.addAttribute("itemType", "Дерево/Куст");
        model.addAttribute("colorHex", intToHexColor(tree.getMarkerColor()));
        model.addAttribute("from", from);
        model.addAttribute("backUrl", getBackUrl(from, tree.getPlotId()));

        model.addAttribute("plantViewUrl", getPlantViewUrl(tree.getPlantId()));

        List<TaskDto.Response> allTasks = taskService.getTasksByUser(userId);
        String targetType = "TREEBUSH";

        List<TaskDto.Response> itemTasks = allTasks.stream()
                .filter(t -> targetType.equals(t.getPlantedItemType()) && id.equals(t.getPlantedItemId()))
                .collect(java.util.stream.Collectors.toList());

        LocalDate now = LocalDate.now();
        List<TaskDto.Response> expandedItemTasks = recurrenceUtils.expandTasks(itemTasks, now.minusMonths(1), now.plusYears(1));

        expandedItemTasks.sort(java.util.Comparator.comparing(TaskDto.Response::getDate));
        model.addAttribute("itemTasks", expandedItemTasks);
        return "garden-item-view";
    }
}