package com.supermap.learning.minIO.service;

import java.util.Map;

/**
 * @author lty
 */
public interface DecompressService {

    void decompress(String target, String fileName);

    Map<String, Integer> state(String fileName);

    Boolean deleteTemp(String fileName);

}
