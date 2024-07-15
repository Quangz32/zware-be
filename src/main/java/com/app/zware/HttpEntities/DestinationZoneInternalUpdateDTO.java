package com.app.zware.HttpEntities;

import lombok.Data;

import java.util.List;

@Data
public class DestinationZoneInternalUpdateDTO {
    private Integer detailId;
    private List<Integer> destinationZones;
}
