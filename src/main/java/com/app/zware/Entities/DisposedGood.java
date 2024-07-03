package com.app.zware.Entities;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity(name = "disposedgoods")
@Data
public class DisposedGood {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  private Integer zone_id;
  private Integer disposal_id;
  private Integer item_id;
  private Integer quantity;
  private String reason;
}
