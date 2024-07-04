package com.app.zware.Controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {


  @GetMapping("")
  public void testMain(){
    System.out.println("Hello from test");
  }

}
