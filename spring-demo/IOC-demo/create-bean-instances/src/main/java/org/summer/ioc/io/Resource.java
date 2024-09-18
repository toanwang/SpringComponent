package org.summer.ioc.io;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Resource{
    private String path;
    private String name;
}
