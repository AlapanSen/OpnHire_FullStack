package controller;

import entity.Seeker;
import entity.SeekerPreference;
import entity.User;
import Repository.SeekerRepository;
import Repository.SeekerPreferenceRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/preferences")
public class SeekerPreferenceController {

    @Autowired
    private SeekerRepository seekerRepository;
    
    @Autowired
    private SeekerPreferenceRepository preferenceRepository;
    
    @GetMapping
    public String showPreferences(HttpSession session, Model model) {
        // Check if user is logged in and is a seeker
        User user = (User) session.getAttribute("user");
        
        if (user == null || !user.getRole().equals(User.UserRole.SEEKER)) {
            return "redirect:/login?error=Access denied. Please log in as a job seeker.";
        }
        
        // Get seeker profile
        Seeker seeker = seekerRepository.findById(user.getId()).orElse(null);
        
        if (seeker == null) {
            return "redirect:/dashboard/seeker?error=Seeker profile not found";
        }
        
        // Get or initialize preferences
        SeekerPreference preference = seeker.getPreference();
        if (preference == null) {
            preference = seeker.initializePreference();
            seekerRepository.save(seeker);
        }
        
        // Add to model
        model.addAttribute("preference", preference);
        
        return "seeker/preferences";
    }
    
    @PostMapping("/update")
    public String updatePreferences(
            @RequestParam(required = false) Double minSalary,
            @RequestParam(required = false) Double maxSalary,
            @RequestParam(required = false) String preferredLocation,
            @RequestParam(required = false) String preferredJobType,
            @RequestParam(required = false) String preferredSkills,
            @RequestParam(required = false) String preferredIndustry,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        // Check if user is logged in and is a seeker
        User user = (User) session.getAttribute("user");
        
        if (user == null || !user.getRole().equals(User.UserRole.SEEKER)) {
            return "redirect:/login?error=Access denied. Please log in as a job seeker.";
        }
        
        // Get seeker profile
        Seeker seeker = seekerRepository.findById(user.getId()).orElse(null);
        
        if (seeker == null) {
            return "redirect:/dashboard/seeker?error=Seeker profile not found";
        }
        
        // Get or initialize preferences
        SeekerPreference preference = seeker.getPreference();
        if (preference == null) {
            preference = seeker.initializePreference();
        }
        
        // Update preferences
        preference.setMinSalary(minSalary);
        preference.setMaxSalary(maxSalary);
        preference.setPreferredLocation(preferredLocation);
        preference.setPreferredJobType(preferredJobType);
        preference.setPreferredSkills(preferredSkills);
        preference.setPreferredIndustry(preferredIndustry);
        
        // Save
        seekerRepository.save(seeker);
        
        // Redirect with success message
        redirectAttributes.addFlashAttribute("message", "Preferences updated successfully!");
        return "redirect:/preferences";
    }
}