// Java Program to Illustrate DemoController

// Importing package to code module
package gov.example.appDemo.controller;
// Importing required classes
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

//import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
//import org.springframework.jdbc.support.SQLExceptionTranslator;

import gov.example.appDemo.AppDemoApplication;

// Annotation
@Controller
@RequestMapping("students")
// Class
public class DemoController {
    @Autowired
    AppDemoApplication App = new AppDemoApplication();
    // Ask degree by name
    @ResponseBody
    @GetMapping("/degree")
    public String ResponseDegree(@RequestParam("name") String name){
        String degree = "";
        try{
            String val = App.ReturnDegree(name);
            degree += val;
        } catch (AppDemoApplication.SameNameSearchedError e){
            return "There are multiple students with the same name. Please provide an email address instead.";
        } catch(AppDemoApplication.NoResultError e){
            return "No such student";
        }

        String ans = name + " : " + degree;
        return ans;
        // error control
        // "No such student"
        // "There are multiple students with the same name. Please provide an email address instead."
    }

    // Ask email by name
    @ResponseBody
    @GetMapping("/email")
    public String ResponseEmail(@RequestParam("name") String name){
        String email = "";
        try{
            String val = App.ReturnEmail(name);
            email += val;
        } catch (AppDemoApplication.SameNameSearchedError e){
            return "There are multiple students with the same name. Please contact the administrator by phone.";
        }  catch(AppDemoApplication.NoResultError e){
            return "No such student";
        }

        String ans = name + " : "  + email;
        return ans;

        // error control
        // "No such student"
        // "There are multiple students with the same name. Please contact the administrator by phone."
    }

    @ResponseBody
    @GetMapping("/stat")
    public String ResponseCount(@RequestParam("degree") String degree) {
        int cnt = App.ReturnCount(degree); // phd, master, undergrad
        String ans = "Number of " + degree + "'s student : " + cnt;
        return ans;
    }

    @ResponseBody
    //@PutMapping("/register")
    @RequestMapping(value="/register", method = {RequestMethod.GET, RequestMethod.PUT})
    public String AddStudent(@RequestParam("name") String name, @RequestParam("email") String email, @RequestParam("graduation") String graduation, @RequestParam("degree") String degree) {
        if( App.SameNameCount(name)>0){
            return "Already registered";
        }
        
        try {
        	App.Insert(name, email, graduation, degree);
        } catch (AppDemoApplication.SameEmailError e) {
        	return "DuplicateKeyException Occured";
        }
        return "Registration successful";
        // "Registration successful"
        // "Already registered"
    }

}

	/*
	@RequestMapping("/hello")
	@ResponseBody

	// Method
	public String helloWorld()
	{
		// Print statement
		return "Hello World!";
	}*/

//@GetMapping("/world")
//@ResponseBody
/*
	public String getWorld (@RequestParam String a) {
		return "Getting the world";
	}*/

//@GetMapping(value = "/test")