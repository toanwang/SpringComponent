package org.summer.ioc.scan.primary;

import org.summer.ioc.annotation.Autowired;
import org.summer.ioc.annotation.Component;

@Component
public class StudentBean extends PersonBean {

    DogBean dogBean;

    public StudentBean(@Autowired DogBean dogBean){
        this.dogBean = dogBean;
    }
}
