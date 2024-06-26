package com.app.zware.Service;

import com.app.zware.Entities.InboundTransaction;
import com.app.zware.Entities.InboundTransactionDetail;
import com.app.zware.Entities.User;
import com.app.zware.HttpEntities.InboundTransactionDTO;
import com.app.zware.Repositories.InboundTransactionDetailRepository;
import com.app.zware.Repositories.InboundTransactionRepository;
import java.util.List;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
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
    // Lấy thông tin người tạo yêu cầu
    User requestMaker = userService.getRequestMaker(request);

    InboundTransaction inboundTransaction = new InboundTransaction();


    inboundTransaction.setDate(inboundTransactionDTO.getDate());
    inboundTransaction.setMaker_id(requestMaker.getId());
    inboundTransaction.setStatus("pending"); // Mặc định là pending
    inboundTransaction.setIsdeleted(false); // Mặc định là false

    // Kiểm tra và thiết lập source hoặc external_source
    if (inboundTransactionDTO.getSource() != null) {
      inboundTransaction.setSource(inboundTransactionDTO.getSource());
    } else {
      inboundTransaction.setExternal_source(inboundTransactionDTO.getExternal_source());
    }

    // Lưu inboundTransaction vào cơ sở dữ liệu
    InboundTransaction savedTransaction = repository.save(inboundTransaction);

    // Lưu các chi tiết giao dịch (details) vào cơ sở dữ liệu
    List<InboundTransactionDetail> details = inboundTransactionDTO.getDetails();
    for (InboundTransactionDetail detail : details) {
      detail.setTransaction_id(savedTransaction.getId());
      inboundTransactionDetailRepository.save(detail);
    }

    // Tạo và trả về InboundTransactionDTO từ savedTransaction và details
    InboundTransactionDTO resultDTO = new InboundTransactionDTO();
    resultDTO.setDate(savedTransaction.getDate());
    resultDTO.setMaker_id(savedTransaction.getMaker_id());
    resultDTO.setStatus(savedTransaction.getStatus());
    resultDTO.setSource(savedTransaction.getSource());
    resultDTO.setExternal_source(savedTransaction.getExternal_source());
    resultDTO.setIsdeleted(savedTransaction.isIsdeleted());
    resultDTO.setDetails(details);

    return resultDTO;
  }


}
