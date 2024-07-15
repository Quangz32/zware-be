package com.app.zware.Service;
import com.app.zware.Entities.DisposedGood;
import com.app.zware.Repositories.DisposedGoodRespository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DisposedGoodService {

  @Autowired
  DisposedGoodRespository disposedGoodRespository;

  public List<DisposedGood> getAllDisposedGood() {
    return disposedGoodRespository.findAll();
  }

  public DisposedGood getDisposedGoodById(Integer id) {
    return disposedGoodRespository.findById(id).orElse(null);
  }

  public List<DisposedGood> getByGoodDisposal(Integer disposalId) {
    return disposedGoodRespository.findByGoodDisposalId(disposalId);
  }

  public DisposedGood createDisposedGood(DisposedGood request) {
    return disposedGoodRespository.save(request);
  }

  public void deleteDisposedGood(Integer id) {
    disposedGoodRespository.deleteById(id);
  }

  public Boolean checkIdExist(Integer id) {
    return disposedGoodRespository.existsById(id);
  }

  public DisposedGood merge(Integer oldDisposedGoodId, DisposedGood newDisposedGood) {
    DisposedGood oldDisposedGood = disposedGoodRespository.findById(oldDisposedGoodId).orElse(null);
    if (oldDisposedGood == null) {
      return null;
    }

    Optional.ofNullable(newDisposedGood.getDisposal_id())
            .ifPresent(oldDisposedGood::setDisposal_id);
    Optional.ofNullable(newDisposedGood.getItem_id()).ifPresent(oldDisposedGood::setItem_id);
    Optional.of(newDisposedGood.getQuantity()).ifPresent(oldDisposedGood::setQuantity);
    Optional.ofNullable(newDisposedGood.getReason()).ifPresent(oldDisposedGood::setReason);

    return oldDisposedGood;
  }

  public DisposedGood update(DisposedGood mergeDisposalGood) {
    return disposedGoodRespository.save(mergeDisposalGood);
  }
}
