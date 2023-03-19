package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Main {
    private static final int PORT = 34522;
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static DataInputStream inputStream;
    private static DataOutputStream outputStream;

    public static void main(String[] args) {
        // todo - your java-code
        launch();
    }

    private static void launch() {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, PORT);
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter action (1 - get a file, 2 - save a file, 3 - delete a file)");
            int action = scanner.nextInt();
            outputStream.writeUTF(String.valueOf(action));
            doAction(action);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private static void doAction(int action) throws IOException {
        switch (action) {
            case (1): {
                Scanner sc = new Scanner(System.in);
                System.out.println("Do you want to get the file by name or by id (1 - name, 2 - id):");
                String recieveAction = sc.nextLine();
                if (recieveAction.equals("1")) {
                    System.out.println("Enter name of file");
                    Scanner sc1 = new Scanner(System.in);
                    String a = sc1.nextLine();
                    outputStream.writeUTF(a);
                    writeBytesInNewFile(a, getArrayOfBytesFromSocketStream());
                    System.out.println(inputStream.readUTF());
                    // получаем по имени
                } else if (recieveAction.equals("2")) {
                    // получаем по id
                    System.out.println("Enter id of file");
                    Scanner sc1 = new Scanner(System.in);
                    String a = sc1.nextLine();
                    outputStream.writeUTF(a);
                    //  String name = "C:\\Users\\fikus\\IdeaProjects\\se-lab04-tmp2223\\src\\main\\java\\client\\data\\" + a;

                    writeBytesInNewFile(inputStream.readUTF(), getArrayOfBytesFromSocketStream());
                    System.out.println(inputStream.readUTF());
                }
                // получили файл
            }
            case (2): {
                System.out.println("Enter name of the file:");
                Scanner sc = new Scanner(System.in);
                String nameOfFile = sc.nextLine();
                outputStream.writeUTF(nameOfFile);
                String a = inputStream.readUTF();
                if (a.equals("File already exist")) {
                    System.out.println("File already exist in server");
                    break;
                } else {
                    System.out.println(a);

                    try {
                        sendFile("C:\\Users\\fikus\\IdeaProjects\\se-lab04-tmp2223\\src\\main\\java\\client\\data\\" + nameOfFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Unique id for " + nameOfFile + " " + inputStream.readUTF());
                    break;
                }
            }
            case (3): {
                Scanner sc = new Scanner(System.in);
                System.out.println("Do you want to delete the file by name or by id (1 - name, 2 - id):");
                String deleteAction = sc.nextLine();
                if (deleteAction.equals("1")) {
                    System.out.println("Enter name of file");
                    Scanner sc1 = new Scanner(System.in);
                    String a = sc1.nextLine();
                    outputStream.writeUTF(a);
                    System.out.println(inputStream.readUTF());
                    // удаляем по имени
                } else if (deleteAction.equals("2")) {
                    // удаляем по id
                    System.out.println("Enter id of file");
                    Scanner sc1 = new Scanner(System.in);
                    String a = sc1.nextLine();
                    outputStream.writeUTF(a);
                    System.out.println(inputStream.readUTF());
                }
                // удалили файл из сервера
            }
        }
    }

    private static byte[] getArrayOfBytesFromSocketStream() throws IOException {
        int length = (int) inputStream.readLong();
        byte[] resultBytes = new byte[length];
        inputStream.readFully(resultBytes, 0, length);
        return resultBytes;
    }

    // по заданному имени создает файл в data и байты из массива записывает в новый файл
    private static void writeBytesInNewFile(String nameOfFile, byte[] bytes) throws FileNotFoundException {
        String pathToNewFile = "C:\\Users\\fikus\\IdeaProjects\\se-lab04-tmp2223\\src\\main\\java\\client\\data\\" + nameOfFile;
        File file = new File(pathToNewFile);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        try {
            fileOutputStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
