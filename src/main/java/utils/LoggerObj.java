package utils;

import com.mongodb.BasicDBObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class LoggerObj {

    final String methodName;
    final HashMap<String, String> fields;
    final String time;
    final String type;

    public LoggerObj(String methodName, HashMap<String, String> fields, String type) {
        this.methodName = methodName;
        this.fields = fields;
        this.time = DateTimeFormatter.
                ofPattern("yyyy/MM/dd HH:mm:ss").
                format(LocalDateTime.now());
        this.type = type;
    }

    public String getAsString() {
        StringBuilder builder = new StringBuilder(methodName);
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            builder.append(
                    String.format(
                            "|%s-%s",
                            entry.getKey(),
                            entry.getValue()
                    )
            );
        }
        return builder.toString();
    }

    public BasicDBObject getDBScheme() {
        BasicDBObject object = new BasicDBObject();
        object.append("type", this.type);
        object.append("methodName", this.methodName);
        object.append("time", this.time);
        if (!this.fields.isEmpty()) {
            for (Map.Entry<String, String> entry : this.fields.entrySet()) {
                object.append(entry.getKey(), entry.getValue());
            }
        }
        return object;
    }
}
