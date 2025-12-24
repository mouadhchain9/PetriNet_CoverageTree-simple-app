package com.svs.dto;

import java.util.List;
import java.util.ArrayList;

public class NetDTO {
    public List<PlaceDTO> places = new ArrayList<>();
    public List<TransitionDTO> transitions = new ArrayList<>();
    public List<ArcDTO> arcs = new ArrayList<>();
}