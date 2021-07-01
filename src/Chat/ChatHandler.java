package Chat;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class ChatHandler implements Runnable {
    private final Socket socket;
    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;
    private static List<ChatHandler> handlers = Collections.synchronizedList(new ArrayList<>());
    private static Set<String> forbiddenWords = Collections.synchronizedSet(new HashSet<>());
    private static volatile int countOfClients;

    public ChatHandler(Socket socket) throws IOException {
        this.socket = socket;
        dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        dataOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        forbiddenWords.add("политика");
        forbiddenWords.add("Путин");
        forbiddenWords.add("Навальный");
    }

    public synchronized static void subtractCount() {
        --countOfClients;
    }

    @Override
    public void run() {
        handlers.add(this);
        ++countOfClients;
        System.out.println("handlers.size() " + handlers.size() + countOfClients);
        try {
            while (true) {
                String textClient = dataInputStream.readUTF();
                String[] splitText = textClient.split(" ");
                for (String s : splitText) {
                    if (forbiddenWords.contains(s)) {
                        throw new ForbiddenWordsException("Запрещенное слово");
                    }
                }
                broadcast(textClient);
            }
        } catch (ForbiddenWordsException e ) {
            e.printStackTrace();
        } catch (IOException e ) {
            e.printStackTrace();
        }finally {
            handlers.remove(this);
            try {
                dataOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void broadcast(String message) {
        synchronized (handlers) {
            Iterator<ChatHandler> iterator = handlers.iterator();
            while (iterator.hasNext()) {
                ChatHandler chatHandler = iterator.next();
                try {
                    synchronized (chatHandler.dataOutputStream) {
                        chatHandler.dataOutputStream.writeUTF(message);
                    }
                    chatHandler.dataOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public synchronized static int getCountOfClients() {
        return countOfClients;
    }

    public class ForbiddenWordsException extends Exception {
        public ForbiddenWordsException(String message) {
            super(message);
        }
    }
}
