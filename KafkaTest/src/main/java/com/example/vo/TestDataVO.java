package com.example.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString
@AllArgsConstructor
public class TestDataVO {
    private String key;
    private long enqueTime;
    private long dequeTime;
    
    public TestDataVO(){
        
    }
}
