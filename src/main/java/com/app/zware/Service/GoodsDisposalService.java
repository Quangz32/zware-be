package com.app.zware.Service;

import com.app.zware.Entities.DisposedGood;
import com.app.zware.Entities.GoodsDisposal;
import com.app.zware.Repositories.DisposedGoodRespository;
import com.app.zware.Repositories.GoodsDisposalRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GoodsDisposalService {

  @Autowired
  GoodsDisposalRepository goodsDisposalRepository;

  public List<GoodsDisposal> findAllGoods() {
    return goodsDisposalRepository.findAll();
  }

  public GoodsDisposal getGoodsById(Integer id) {
    return goodsDisposalRepository.findById(id).orElse(null);
  }

  public GoodsDisposal createGoodsDisposed(GoodsDisposal request) {
    return goodsDisposalRepository.save(request);
  }

  public void deleteById(Integer id) {
    GoodsDisposal goodsDisposal = getGoodsById(id);
    goodsDisposal.setIsdeleted(true);
    goodsDisposalRepository.save(goodsDisposal);

    //goodsDisposalRepository.deleteById(id);
  }

  public Boolean checkIdExist(Integer id) {
    return goodsDisposalRepository.existsById(id);
  }


  public GoodsDisposal merge(Integer oldGoodsDisposalId, GoodsDisposal newGoodsDisposal) {
    GoodsDisposal oldGoodsDisposal = goodsDisposalRepository.findById(oldGoodsDisposalId)
        .orElse(null);
    if (oldGoodsDisposal == null) {
      return null;
    }

    Optional.ofNullable(newGoodsDisposal.getWarehouse_id())
        .ifPresent(oldGoodsDisposal::setWarehouse_id);
    Optional.ofNullable(newGoodsDisposal.getDate()).ifPresent(oldGoodsDisposal::setDate);
    Optional.ofNullable(newGoodsDisposal.getStatus()).ifPresent(oldGoodsDisposal::setStatus);
    Optional.ofNullable(newGoodsDisposal.getMaker_id()).ifPresent(oldGoodsDisposal::setMaker_id);

    return oldGoodsDisposal;
  }

  public GoodsDisposal update(GoodsDisposal mergeGoodsDisposal) {
    return goodsDisposalRepository.save(mergeGoodsDisposal);
  }

  public List<GoodsDisposal> getByWarehouse(Integer warehouseId){
    return goodsDisposalRepository.findByWarehouse(warehouseId);
  }
}
