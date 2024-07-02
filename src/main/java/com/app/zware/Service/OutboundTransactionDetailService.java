package com.app.zware.Service;

import com.app.zware.Entities.OutboundTransaction;
import com.app.zware.Entities.OutboundTransactionDetail;
import com.app.zware.Repositories.OutboundTransactionDetailRepository;
import com.app.zware.Repositories.OutboundTransactionRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OutboundTransactionDetailService {

  @Autowired
  OutboundTransactionDetailRepository outboundTransactionDetailRepository;

  @Autowired
  OutboundTransactionRepository outboundTransactionRepository;

  public List<OutboundTransactionDetail> getAll() {
    return outboundTransactionDetailRepository.findAll();
  }

  public OutboundTransactionDetail create(OutboundTransactionDetail request) {
    return outboundTransactionDetailRepository.save(request);
  }

  public OutboundTransactionDetail save(OutboundTransactionDetail detail) {
    return outboundTransactionDetailRepository.save(detail);
  }

  public boolean checkExistId(Integer id) {
    return outboundTransactionDetailRepository.existsById(id);
  }

  public void delete(Integer id) {
    outboundTransactionDetailRepository.deleteById(id);
  }

  public OutboundTransactionDetail getById(Integer id) {
    return outboundTransactionDetailRepository.findById(id).orElse(null);
  }

  public OutboundTransaction getTransaction(OutboundTransactionDetail detail) {
    return outboundTransactionRepository.findById(detail.getTransaction_id()).orElse(null);
  }

  public OutboundTransactionDetail update(OutboundTransactionDetail outboundTransactionDetail) {
    return outboundTransactionDetailRepository.save(outboundTransactionDetail);
  }

  public OutboundTransactionDetail merge(Integer id, OutboundTransactionDetail request) {
    OutboundTransactionDetail oldOutboundTransactionDetail = getById(id);
    if (oldOutboundTransactionDetail == null) {
      return null;
    }
    OutboundTransactionDetail outboundTransactionDetail = getById(id);
    Optional.ofNullable(request.getTransaction_id())
        .ifPresent(outboundTransactionDetail::setTransaction_id);
    Optional.ofNullable(request.getItem_id()).ifPresent(outboundTransactionDetail::setItem_id);
    Optional.of(request.getQuantity()).ifPresent(outboundTransactionDetail::setQuantity);
    Optional.ofNullable(request.getZone_id()).ifPresent(outboundTransactionDetail::setZone_id);
    return oldOutboundTransactionDetail;
  }

  public List<OutboundTransactionDetail> getByOutboundTransaction(Integer transactionId){
    return outboundTransactionDetailRepository.findByOutboundTransaction(transactionId);
  }
}
