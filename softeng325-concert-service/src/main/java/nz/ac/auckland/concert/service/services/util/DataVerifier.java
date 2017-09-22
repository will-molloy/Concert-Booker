package nz.ac.auckland.concert.service.services.util;

import nz.ac.auckland.concert.common.dto.UserDTO;
import nz.ac.auckland.concert.service.domain.User;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

/**
 * Utility class that contains methods to verify certain statistics about domain and DTO objects.
 *
 * @author Will 
 */
public class DataVerifier {

    /**
     * Determines if all fields are set for an object i.e. they're not null.
     * @param obj, an instance of some class
     * @return true if all fields are not null
     */
    public static boolean allFieldsAreSet(Object obj){
        for (Field field : obj.getClass().getDeclaredFields()){
            field.setAccessible(true);
            try {
                if (Objects.isNull(field.get(obj))){
                    return false;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
