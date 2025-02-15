package com.gtl.process;

import lombok.Data;

import java.util.Map;

@Data
public class ProcessRequest {
    private Map<String, Object> data;
}
