package com.app.zware.Validation;

import com.app.zware.Repositories.InternalTransactionDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InternalTransactionDetailValidator {
    @Autowired
    InternalTransactionDetailRepository internalTransactionDetailRepository;

    public String checkGet(Integer internalDetailId){
        if(!internalTransactionDetailRepository.existsById(internalDetailId)){
            return "Not Found by Internal Transaction Id";
        }
        return "";
    }
}
