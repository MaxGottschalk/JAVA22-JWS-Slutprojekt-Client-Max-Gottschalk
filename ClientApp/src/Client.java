import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.sql.SQLOutput;
import java.util.Scanner;
import java.util.Set;

public class Client {
    public static void main(String[] args) {

        Socket socket = null;
        InputStreamReader inputStreamReader = null;
        OutputStreamWriter outputStreamWriter = null;
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;

        Scanner sc = new Scanner(System.in);

        try {
            socket = new Socket("localhost", 6969);

            inputStreamReader = new InputStreamReader(socket.getInputStream());
            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());

            //Initierar Reader och Writer och kopplar dem till socket
            bufferedReader = new BufferedReader(inputStreamReader);
            bufferedWriter = new BufferedWriter(outputStreamWriter);

            while (true) {

                //Meddelandet som ska skickas till servern
                String message = userInput();

                //Skicka meddelande till server
                bufferedWriter.write(message);
                bufferedWriter.newLine();
                bufferedWriter.flush();

                String resp = bufferedReader.readLine();

                //Tar emot serverns response
                JSONObject persons = openResponse(resp);

                String choice = sc.nextLine();
                int number = Integer.parseInt(choice);

                //Skriver ut specifika personers detaljer
                if (number <= 10){
                    String p = "p" + number;
                    JSONObject person = (JSONObject) persons.get(p);

                    System.out.println("Name: " + person.get("name") + ", age: " + person.get("age"));
                    System.out.println("'" + person.get("quote") + "'");

                } else if (number == 11){
                    Set<String> keys = persons.keySet();
                    int ct = 1;
                    for (String x : keys) {
                        JSONObject person = (JSONObject) persons.get(x);

                        //Skriv ut namn på alla personer
                        System.out.println(ct + "." + person.get("name"));
                        ct++;
                    }
                }
                System.out.print("Press any button to continue...");
                sc.nextLine();

                //Avsluta om användaren skriver q
                if (message.equalsIgnoreCase("q"))
                    break;
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            //Stäng kopplingar
            try {
                if (socket != null) {
                    socket.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (outputStreamWriter != null) {
                    outputStreamWriter.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
            } catch (Exception e) {
                System.out.println(e);
                System.out.println("Klienten Avslutas!");
            }
        }
    }

    static String userInput(){
        //Steg 1. Skriv ut en meny för användaren
        System.out.println("Select your option, to see which clone to access press 11.");
        System.out.println("Get data of any specific clone. Options 1-10 in the database.");

        System.out.println("11. Get the names of all the Clone Troopers.");


        //Steg 2. Låta användaren göra ett val
        System.out.print("Make your choice: ");

        //Skapa JSON objekt för att hämta data om alla personer. Stringifiera objektet och returnerar det
        JSONObject jsonReturn = new JSONObject();
        jsonReturn.put("httpURL", "persons");
        jsonReturn.put("httpMethod", "get");

        //Returnera JSON objekt
        return jsonReturn.toJSONString();

    }

    static JSONObject openResponse(String resp) throws ParseException {

        //Initierar parser för att parsa till JSON
        JSONParser parser = new JSONParser();

        JSONObject serverResponse = (JSONObject) parser.parse(resp);

        //Kollar om respons lyckas
        if ("200".equals(serverResponse.get("httpStatusCode").toString())){

            //Bygger upp ett JSON-objekt av den returnerade data
            JSONObject data = (JSONObject) parser.parse((String) serverResponse.get("data"));

            return data;
        }
        return null;
    }
}