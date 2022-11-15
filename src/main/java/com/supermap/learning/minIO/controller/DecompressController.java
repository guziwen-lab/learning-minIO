package com.supermap.learning.minIO.controller;

import com.supermap.learning.minIO.service.DecompressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author lty
 */
@RestController
public class DecompressController {

    @Autowired
    private DecompressService decompressService;

    @GetMapping("decompress")
    public void decompress(@RequestParam String target, @RequestParam String fileName) {
        decompressService.decompress(target, fileName);
    }

    @GetMapping("state")
    public Map<String, Integer> state(@RequestParam String fileName) {
        return decompressService.state(fileName);
    }

    @DeleteMapping("delete")
    public Boolean delete(@RequestParam String fileName) {
        return decompressService.deleteTemp(fileName);
    }

}
