package com.app.zware.Service;

import com.app.zware.Entities.InboundTransaction;
import com.app.zware.Entities.InboundTransactionDetail;
import com.app.zware.Repositories.InboundTransactionDetailRepository;
import com.app.zware.Repositories.InboundTransactionRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InboundTransactionDetailService {

  @Autowired
  InboundTransactionDetailRepository detailRepository;

  @Autowired
  InboundTransactionRepository transactionRepository;

  public List<InboundTransactionDetail> getAll() {
    return detailRepository.findAll();
  }

  public InboundTransactionDetail getById(int id) {
    return detailRepository.findById(id).orElse(null);
  }

  public InboundTransaction getTransaction(InboundTransactionDetail detail) {
    return transactionRepository.findById(detail.getTransaction_id()).orElse(null);
  }

  public InboundTransactionDetail save(InboundTransactionDetail detail) {
    detailRepository.save(detail);
    return detail;
  }

  public InboundTransactionDetail update(Integer id, InboundTransactionDetail mergedDetail) {
    if (id.equals(mergedDetail.getTransaction_id())) {
      return detailRepository.save(mergedDetail);
    }
    return null;
  }

  public InboundTransactionDetail merge(Integer oldDetailId, InboundTransactionDetail newDetail) {
    InboundTransactionDetail oldDetail = detailRepository.findById(oldDetailId).orElse(null);
    if (oldDetail == null) {
      return null;
    }

    Optional.ofNullable(newDetail.getTransaction_id()).ifPresent(oldDetail::setTransaction_id);
    Optional.of(newDetail.getQuantity()).ifPresent(oldDetail::setQuantity);
    Optional.ofNullable(newDetail.getItem_id()).ifPresent(oldDetail::setItem_id);
    Optional.ofNullable(newDetail.getZone_id()).ifPresent(oldDetail::setZone_id);

    return oldDetail; //Has been UPDATED
  }

  public void delete(Integer id) {
    detailRepository.deleteById(id);
  }

  public List<InboundTransactionDetail> findByInboundTransactionId (Integer transactionId){
    return detailRepository.findByInboundTransactionId(transactionId);
  }
}



