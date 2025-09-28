package com.campusjot.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.campusjot.project.model.Client;
import com.campusjot.project.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class LogControllers {

	public HttpSession session;
	
	@Autowired
	UserService userService;
   
	@PostMapping("/register")
	public RedirectView register(Client u) {
		RedirectView rd=new RedirectView();
		userService.saveUser(u);
		rd.setUrl("signin");
		return rd;
	}

	@PostMapping("/login")
	public ModelAndView login(@RequestParam String username, @RequestParam String password,HttpSession ses) {
		
		this.session=ses;
	    ModelAndView md = new ModelAndView();
	    // Call the new method
	    Client u = userService.getByUsernameWithGroups(username); 

	    if (u != null && u.getPassword().equals(password)) {
	        session.setAttribute("username", username);
	        md.addObject("success", true);

	       
	        if (u.getGroups().isEmpty()) {
	            System.out.println("Login success: User has NO groups. Redirecting to user_home.");
	            userService.setSession(ses);
	            md.setViewName("user_home");
	        } else {
	            System.out.println("Login success: User HAS groups. Redirecting to group page.");
	            md.addObject("groups", u.getGroups());
	            md.setViewName("group");
	        }
	    } else {
	        System.out.println("Login failed for username: " + username);
	        md.addObject("errorMsg", "Invalid Credentials");
	        md.setViewName("signin");
	    }
	    
	    return md;
	}

	@GetMapping("/logout")
	public RedirectView logout() {
		RedirectView rd=new RedirectView("home");
		session.invalidate();
		return rd;
	}
	
	
}
