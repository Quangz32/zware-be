package com.app.zware.HttpEntities;

import java.util.ArrayList;
import lombok.Data;

@Data
public class GoodsDisposalDTO {
  private Integer warehouse_id;
  private  ArrayList<DisposedGoodsDTO> details;

}
