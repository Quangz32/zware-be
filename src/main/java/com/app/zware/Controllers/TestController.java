package com.app.zware.Controllers;

import com.app.zware.Service.InternalTransactionDetailService;
import com.app.zware.Service.InternalTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {
@Autowired
  InternalTransactionService internalTransactionService;

@Autowired
  InternalTransactionDetailService internalTransactionDetailService;

  @GetMapping("")
  public void testMain(){
    System.out.println("Hello from test");
    System.out.println(internalTransactionService.getAll());
    System.out.println(internalTransactionDetailService.getAll());
  }

}
