package com.sai.slog.app.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by saipkri on 13/01/17.
 */
public final class GenericUtil {

    public static void main(String[] args) throws Exception {
        String s = "[] ddd  dkmd 12 kj33j 112ln \n CSCva07592 SN for stackable switches not appearing PI 3.0(3) Upd\n";
        Matcher matcher = Pattern.compile("CSC(.*?)\\d+").matcher(s);

        while (matcher.find()) {
            System.out.println(matcher.group());
        }
    }
}
