package com.app.zware.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import java.util.Date;
import lombok.Data;

@Entity(name = "goodsdisposal")
@Data
public class GoodsDisposal {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private Integer warehouse_id;
  private Integer maker_id;
  private LocalDate date;
  private String status;
  private boolean isdeleted = false;

}
