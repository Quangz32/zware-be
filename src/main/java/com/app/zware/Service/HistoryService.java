package com.app.zware.Service;

import com.app.zware.Entities.History;
import com.app.zware.Repositories.HistoryRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HistoryService {

  @Autowired
  HistoryRepository historyRepository;

  public List<History> getByWarehouseAndProduct(Integer warehouseId, Integer productId) {
    return historyRepository.findByWarehouseAndProduct(warehouseId, productId);
  }

  public List<History> getByWarehouseAndProductAndDateInterval(Integer warehouseId,
      Integer productId, LocalDate startDate, LocalDate endDate) {
    return historyRepository.findByWarehouseAndProductAndDateInterval(warehouseId, productId,
        startDate, endDate);
  }

  public Integer getLastQuantityBeforeDate(Integer warehouseId,
      Integer productId, LocalDate date ){
    History history = historyRepository.findLastBeforeDate(warehouseId, productId, date);

    if (history == null) return 0;
    return history.getQuantity();
  }
}
