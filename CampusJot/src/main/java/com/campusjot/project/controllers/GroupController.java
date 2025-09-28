package com.campusjot.project.controllers;

import com.campusjot.project.dto.CreateGroupRequest;
import com.campusjot.project.dto.JoinGroupRequest;
import com.campusjot.project.model.Client;
import com.campusjot.project.model.Group;
import com.campusjot.project.model.Subjects;
import com.campusjot.project.repo.UserRepo;
import com.campusjot.project.service.GroupService;
import com.campusjot.project.service.SubjectService;
import com.campusjot.project.service.UserService;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
public class GroupController {

	@Autowired
	private GroupService groupService;

	@Autowired
	private SubjectService subjectService;

	@Autowired
	private UserService userService;
	@Value("${supabase.url}")
	private String SUPABASE_URL;
	@Value("${supabase.bucket}")
	private String BUCKET_NAME;

	@Autowired
	private UserRepo clientRepo;

	@PostMapping("/group/create")
	public String createGroup(@ModelAttribute CreateGroupRequest request, HttpSession session,
			RedirectAttributes redirectAttributes, Model m) {

		// Get the owner's username from the session
		String uname = (String) session.getAttribute("username");
		if (uname == null) {
			// Handle case where user is not logged in
			return "signin";
		}

		// 1. Find the full Client object for the owner
		Client owner = clientRepo.findByUsername(uname)
				.orElseThrow(() -> new RuntimeException("Owner not found: " + uname));

		request.getContributorEmails().add(owner.getEmail());

		try {
			// 2. Call your service to create the group
			groupService.createGroup(request.getGroupName(), owner, request.getContributorEmails());

			redirectAttributes.addFlashAttribute("successMessage", "Group created successfully!");
			List<Group> groups = groupService.getGroups(uname);
			for(Group g:groups) {
				System.out.println(g.getGname());
			}
			m.addAttribute("groups", groups);
			return "group"; // Redirect on success

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Error creating group: " + e.getMessage());
			return "create-group"; // Redirect back to the form on error
		}
	}

	@GetMapping("/getmygroups/{username}")
	public ModelAndView getAllGroups(@PathVariable String username, HttpSession session) {
		ModelAndView md = new ModelAndView();
		List<Group> groups = groupService.getAllGroups(username);
		md.addObject("groups", groups);
		md.setViewName("index");
		return md;
	}

	@PostMapping("/group/{groupname}/create-subject")
	public String createSubject(@PathVariable String groupname, @RequestParam String subjectName) {

		groupService.addSubjectToGroup(groupname, subjectName);

		return "redirect:/group/opengroup/" + groupname;
	}

	@PostMapping("/group/join")
	public String joinGroup(@RequestParam("uname") String username, @RequestParam("gid") String group,
			RedirectAttributes redirectAttributes) {

		String groupname = groupService.getGroupById(group).get().getGname();

		if (groupService.addContributorToGroup(group, username)) {
			redirectAttributes.addFlashAttribute("successMessage", "Participant Added Successfully");
			return "redirect:/group/opengroup/" + groupname + "#";
		}
		redirectAttributes.addFlashAttribute("errorMessage", "Provided Username does not Exist");
		return "redirect:/group/opengroup/" + groupname + "#";

	}

	@GetMapping("/group/opengroup/{gname}")
	public ModelAndView openGroup(@PathVariable("gname") String groupname) {

		ModelAndView md = new ModelAndView("subjects"); 

		String gcode = groupname.split("_")[1];

		Group group = groupService.findGroupAndContributors(gcode);

		md.addObject("gname", group.getGname());
		md.addObject("subjects", group.getSubjects());
		md.addObject("contributors", group.getContributors());
		md.addObject("gcode", gcode);
		md.addObject("grpname", groupname);
		System.out.println("Group Code " + gcode);
		return md;
	}

	@PostMapping("/group/joingroup")
	public String joinGroup(@ModelAttribute JoinGroupRequest request,Model model,HttpSession session) {
		
		
		Optional<Group> groupOptional = groupService.getGroupById(request.getGid());
		
		if (groupOptional.isEmpty()) {
			model.addAttribute("errorMsg", "Group not exist");
			return "group";
		}

		Client user = userService.getbyUserName(request.getUname());
		if (user == null) {
			model.addAttribute("errorMsg", "User Not Found");
			return "group";
		}

		Group groupToJoin = groupOptional.get();
		
		if (groupToJoin.getContributors().contains(user)) {
			
			model.addAttribute("errorMsg", "User Already in the Group");
			return "group";
		}

		groupToJoin.addContributor(user);
		groupService.saveGroup(groupToJoin);
		List<Group> groups = groupService.getGroups(request.getUname());
		groups.add(groupToJoin);
		model.addAttribute("successMsg", "You Joined "+request.getGname());
		  for(Group g:groups) {
	        	System.out.println(g.getGname());
	        }
		model.addAttribute("groups", groups);
		return "group";
	}

	@GetMapping("/group/joinToGroup")
	public String joinToGroup() {
		return "join-group";
	}

	@GetMapping("/group/creategroup")
	public String createGroupRequest() {
		return "create-group";
	}
	
	
	@GetMapping("/groups")	
	public String showGroupPage(HttpSession session, Model model) {
	    String username = (String) session.getAttribute("username");
	    if (username != null) {
	        List<Group> groups = groupService.getGroups(username);
	        for(Group g:groups) {
	        	System.out.println(g.getGname());
	        }
	        model.addAttribute("groups", groups);
	    }	
	    return "group";
	}
	
	@GetMapping("/group/leave/{gid}")
	@Transactional
	public String leaveGroup(@PathVariable("gid") String groupId, HttpSession session, RedirectAttributes redirectAttributes) {

	    try {
	        String username = (String) session.getAttribute("username");
	        Client client = userService.getByUserNameOnly(username);
	        Optional<Group> groupOptional = groupService.getGroupById(groupId);

	        if (groupOptional.isEmpty()) {
	            redirectAttributes.addFlashAttribute("errorMsg", "Group was not found.");
	            return "redirect:/groups";
	        }

	        Group g = groupOptional.get();

	        if (g.getContributors().remove(client) && client.getGroups().remove(g)) {
	            groupService.save(g);
	            redirectAttributes.addFlashAttribute("successMsg", "You have successfully left the group.");
	        } else {
	             redirectAttributes.addFlashAttribute("errorMsg", "Error leaving the group.");
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	        redirectAttributes.addFlashAttribute("errorMsg", "An unexpected error occurred.");
	    }

	    return "redirect:/groups";
	}
	
	@GetMapping("/group/delete/{gid}")
	public ModelAndView deleteGroup(@PathVariable String gid,HttpSession session) {
		ModelAndView md=new ModelAndView("group");
		String uid=(String) session.getAttribute("username");
		List<Group> groups;
		if(groupService.deleteGroupById(gid)) {	
			groups=groupService.getGroups(uid);
			md.addObject("groups", groups);
			md.addObject("successMsg", "Group Deleted Successfully");
			return md;
		}
		
		groups=groupService.getGroups(uid);
		md.addObject("errorMsg", "Internal server Error");
		md.addObject("groups", groups);
		return md;
	}
	 
}