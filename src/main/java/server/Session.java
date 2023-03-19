package server;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class Session extends Thread {
    //
    private final Socket socket;
    static DataOutputStream outputStream;
    static DataInputStream inputStream;
    static Map<String, String> idAndNameOfFile;

    public Session(Socket socket) throws IOException {
        this.socket = socket;
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
        idAndNameOfFile = getNameAndId();
    }

    public void run() {
        try {
            int action = Integer.parseInt(inputStream.readUTF());
            doAction(action);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // из соекетового потока получаем массив байтов
    private static byte[] getArrayOfBytesFromSocketStream() throws IOException {
        int length = (int) inputStream.readLong();
        byte[] resultBytes = new byte[length];
        inputStream.readFully(resultBytes, 0, length);
        return resultBytes;
    }

    // по заданному имени создает файл в data и байты из массива записывает в новый файл
    private static void writeBytesInNewFile(String nameOfFile, byte[] bytes) throws FileNotFoundException {
        String pathToNewFile = "C:\\Users\\fikus\\IdeaProjects\\se-lab04-tmp2223\\src\\main\\java\\server\\data\\" + nameOfFile;
        File file = new File(pathToNewFile);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        try {
            fileOutputStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void doAction(int action) throws IOException {
        switch (action) {
            case (1): {
                String idOrName = inputStream.readUTF();
                if (isNumeric(idOrName)) {
                    if (idAndNameOfFile.containsValue(idOrName)) {
                        File file = new File("C:\\Users\\fikus\\IdeaProjects\\se-lab04-tmp2223\\src\\main\\java\\server\\data\\" + getKey(idAndNameOfFile, idOrName));
                        outputStream.writeUTF(getKey(idAndNameOfFile, idOrName));
                        sendFile(file.getPath());
                    }
                } else {
                    File file = new File("C:\\Users\\fikus\\IdeaProjects\\se-lab04-tmp2223\\src\\main\\java\\server\\data\\" + idOrName);
                    sendFile(file.getPath());
                }
              break;

                // по сокету возвращаем файл
            }
            case (2): {
                String name = inputStream.readUTF();
                if (idAndNameOfFile.containsKey(name)) {
                    outputStream.writeUTF("File already exist");
                    break;
                } else {
                    outputStream.writeUTF("File will send");
                    writeBytesInNewFile(name, getArrayOfBytesFromSocketStream());
                    // сохранили idшник
                    String id = generateUniqueValueOfFile();
                    outputStream.writeUTF(id);
                    addInDataBasedNewId(name, id);
                    updateDataBased();
                }
            }
            case (3): {
                String idOrName = inputStream.readUTF();
                if (isNumeric(idOrName)) {
                    if (idAndNameOfFile.containsValue(idOrName)) {
                        File file = new File("C:\\Users\\fikus\\IdeaProjects\\se-lab04-tmp2223\\src\\main\\java\\server\\data\\" + getKey(idAndNameOfFile, idOrName));
                        if (file.delete()) {
                           idAndNameOfFile = rewriteDataBase(idOrName);
                            updateDataBased();
                            outputStream.writeUTF("File of ID " + idOrName + " were successful delet");
                        } else {
                            outputStream.writeUTF("File of ID " + idOrName + " dont were successful delet");
                        }
                    }
                } else {
                    File file = new File("C:\\Users\\fikus\\IdeaProjects\\se-lab04-tmp2223\\src\\main\\java\\server\\data\\" + idOrName);
                    if (file.delete()) {
                        idAndNameOfFile = rewriteDataBase(idOrName);
                        updateDataBased();
                        outputStream.writeUTF("File of name " + idOrName + " were successful delet");
                    } else {
                        outputStream.writeUTF("File of name " + idOrName + "dont were successful delet");
                    }
                }

                // удаляем переданный файл
            }
        }
    }

    // новый уникальный id
    private static String generateUniqueValueOfFile() {
        boolean found = false;
        String candidate = null;
        while (!found) {
            candidate = String.valueOf((int) (Math.random() * 100));
            if (!idAndNameOfFile.containsValue(candidate)) {
                found = true;
            }
        }
        return candidate;
    }

    private static void addInDataBasedNewId(String nameOfFile, String id) {
        idAndNameOfFile.put(nameOfFile, id);
    }

    // записываем текущюу мапу в базу данных
    private static void updateDataBased() throws IOException {
        FileWriter dataBased = null;
        try {
            dataBased = new FileWriter("C:\\Users\\fikus\\IdeaProjects\\se-lab04-tmp2223\\src\\main\\java\\server\\data\\bd.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (Map.Entry<String, String> entry : idAndNameOfFile.entrySet()) {
            System.out.println(entry.getKey() + entry.getValue());
            dataBased.write(entry.getKey() + " " + entry.getValue() + "\n");
        }
        dataBased.close();
        // каждый раз обновляем
        idAndNameOfFile = getNameAndId();
    }

    // вовзращение мапа из бд - ключи имена значения ид
    public static Map<String, String> getNameAndId() {
        Map result = new HashMap();
        File file = new File("C:\\Users\\fikus\\IdeaProjects\\se-lab04-tmp2223\\src\\main\\java\\server\\data\\bd.txt");
        Scanner sc;
        try {
            sc = new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        while (sc.hasNextLine()) {
            String str = sc.nextLine();
            result.put(str.split(" ")[0], str.split(" ")[1]);
        }
        return result;
    }

    // чисто для проверки число ли строка
    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // чтобы достать ключ по значению
    public static <K, V> K getKey(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private static void sendFile(String fileName) throws IOException {
        File file = new File(fileName);
        FileInputStream fileInputStream = new FileInputStream(file);
        // отправляем через сокет длину файла
        outputStream.writeLong(file.length());
        // тут массив может быть очень большой, необходимо будет это учитывать потом
        byte[] buffer = new byte[(int) file.length()];
        // в переменнную буфер считываем все байты из файла
        fileInputStream.read(buffer);
        for (int i = 0; i < buffer.length; i++) {
            System.out.println(buffer[i]);
        }
        // отправили через сокет
        outputStream.write(buffer);
    }
    public static Map<String, String> rewriteDataBase(String nameOrId){
        Map<String, String> map = new HashMap<>();
        List<String> keys = new ArrayList<String>(idAndNameOfFile.keySet());
        for(int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = idAndNameOfFile.get(key);
            if( ! (key.equals(nameOrId) || value.equals(nameOrId))){
                map.put(key, value);
            }
        }
        return map;
    }

}
