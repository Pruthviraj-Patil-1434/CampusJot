package com.campusjot.project.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
		
	@GetMapping("/home")
	public String home() {
		return "index";
	}
	
	@GetMapping("/userhome")
	public String userHome() {
		return "user_home";
	}
	
	@GetMapping("/signin")
	public String signIn() {
		return "signin";
	}
	
	@GetMapping("/signup")
	public String signUp() {
			return "signup";
	}
	
	@GetMapping("/group")
	public String group() {
		return "group";
	}

}
