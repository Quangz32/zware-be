package com.app.zware.Validation;

import com.app.zware.Service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HistoryValidator {
  @Autowired
  HistoryService historyService;


}
