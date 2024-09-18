package org.summer.ioc.io;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PropertyExpr{
    public String key;
    public String defaultValue;
}