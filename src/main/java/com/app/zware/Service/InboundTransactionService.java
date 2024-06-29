package com.app.zware.Service;

import com.app.zware.Entities.InboundTransaction;
import com.app.zware.Entities.InboundTransactionDetail;
import com.app.zware.Entities.Item;
import com.app.zware.Entities.User;
import com.app.zware.HttpEntities.InboundDetailsDTO;
import com.app.zware.HttpEntities.InboundTransactionDTO;
import com.app.zware.Repositories.InboundTransactionDetailRepository;
import com.app.zware.Repositories.InboundTransactionRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.app.zware.Repositories.ItemRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InboundTransactionService {

    @Autowired
    InboundTransactionRepository repository;

    @Autowired
    InboundTransactionDetailRepository inboundTransactionDetailRepository;

    @Autowired
    UserService userService;

    @Autowired
    ItemRepository itemRepository;


    public List<InboundTransaction> getAll() {
        return repository.findAll();
    }

    public InboundTransaction getById(int id) {
        return repository.findById(id).orElse(null);
    }

    public InboundTransaction save(InboundTransaction transaction) {
        InboundTransaction inboundTransaction = new InboundTransaction();
        inboundTransaction.setDate(transaction.getDate());
        inboundTransaction.setMaker_id(transaction.getMaker_id());
        inboundTransaction.setStatus(transaction.getStatus());
        inboundTransaction.setSource(transaction.getSource());
        inboundTransaction.setExternal_source(transaction.getExternal_source());
        return repository.save(inboundTransaction);

    }

    public InboundTransaction update(InboundTransaction mergedTransaction) {
        return repository.save(mergedTransaction);
    }

    public InboundTransaction merge(Integer oldTransactionId, InboundTransaction newTransaction) {
        InboundTransaction oldTransaction = repository.findById(oldTransactionId).orElse(null);
        if (oldTransaction == null) {
            return null;
        }

        Optional.ofNullable(newTransaction.getDate()).ifPresent(oldTransaction::setDate);
        Optional.ofNullable(newTransaction.getMaker_id()).ifPresent(oldTransaction::setMaker_id);
        Optional.ofNullable(newTransaction.getStatus()).ifPresent(oldTransaction::setStatus);
        Optional.ofNullable(newTransaction.getSource()).ifPresent(oldTransaction::setSource);
        Optional.ofNullable(newTransaction.getExternal_source())
                .ifPresent(oldTransaction::setExternal_source);

        return oldTransaction; //has been UPDATED
    }

    public void delete(Integer id) {
        InboundTransaction inboundTransaction = getById(id);
        inboundTransaction.setIsdeleted(true);
        repository.save(inboundTransaction);

        //repository.deleteById(id);
    }

    public List<InboundTransactionDetail> getInboundDetailsByTransactionId(Integer transactionId) {
        return inboundTransactionDetailRepository.findByInboundTransactionId(transactionId);
    }


    //create inboundtransaction and list details
    public InboundTransactionDTO createInboundTransaction(InboundTransactionDTO inboundTransactionDTO, HttpServletRequest request) {
        // Get request maker information
        User requestMaker = userService.getRequestMaker(request);

        // Create and save InboundTransaction entity
        InboundTransaction inboundTransaction = new InboundTransaction();
        inboundTransaction.setDate(inboundTransactionDTO.getDate());
        inboundTransaction.setMaker_id(requestMaker.getId());
        inboundTransaction.setWarehouse_id(inboundTransactionDTO.getWarehouse_id());
        inboundTransaction.setStatus("pending"); // Default to pending
        inboundTransaction.setIsdeleted(false); // Default to false

        // Set source or external_source based on DTO values
        if (inboundTransactionDTO.getSource() != null) {
            inboundTransaction.setSource(inboundTransactionDTO.getSource());
        } else {
            inboundTransaction.setExternal_source(inboundTransactionDTO.getExternal_source());
        }

        // Save inboundTransaction to database
        InboundTransaction savedTransaction = repository.save(inboundTransaction);

        List<InboundDetailsDTO> inboundDetailsDTOS = inboundTransactionDTO.getDetails();
        for (InboundDetailsDTO detailsDTO : inboundDetailsDTOS) {
            // Check if item exists in the database or create new item if it doesn't exist
            Item item = itemRepository.findOptionalByProductIdAndExpiredDate(detailsDTO.getProduct_id(), detailsDTO.getExpire_date())
                    .orElseGet(() -> {
                        Item newItem = new Item();
                        newItem.setProduct_id(detailsDTO.getProduct_id());
                        newItem.setExpire_date(detailsDTO.getExpire_date());
                        return itemRepository.save(newItem);
                    });
            // Create and save InboundTransactionDetail entity
            InboundTransactionDetail detail = new InboundTransactionDetail();
            detail.setTransaction_id(savedTransaction.getId());
            detail.setItem_id(item.getId());
            detail.setQuantity(detailsDTO.getQuantity());
            detail.setZone_id(detailsDTO.getZone_id());

            inboundTransactionDetailRepository.save(detail);
        }
        // Return DTO with saved transaction details
        inboundTransactionDTO.setMaker_id(requestMaker.getId());
        inboundTransactionDTO.setWarehouse_id(requestMaker.getWarehouse_id());
        inboundTransactionDTO.setStatus("pending");
        return inboundTransactionDTO;
    }



}




