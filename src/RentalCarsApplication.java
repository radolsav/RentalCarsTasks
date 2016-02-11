import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * Created by User on 10/02/2016.
 */
public class RentalCarsApplication {

  private static final Map<String, String> carTypeMap;

  static {
    Map<String, String> tempCarTypeMap = new HashMap<>();
    tempCarTypeMap.put("M", "Mini");
    tempCarTypeMap.put("E", "Economy");
    tempCarTypeMap.put("C", "Compact");
    tempCarTypeMap.put("I", "Intermediate");
    tempCarTypeMap.put("S", "Standard");
    tempCarTypeMap.put("F", "Full size");
    tempCarTypeMap.put("P", "Premium");
    tempCarTypeMap.put("L", "Luxury");
    tempCarTypeMap.put("X", "Special");
    carTypeMap = Collections.unmodifiableMap(tempCarTypeMap);
  }

  private static final Map<String, String> doorsCarTypeMap;

  static {
    Map<String, String> tempDoorsCarTypeMap = new HashMap<>();
    tempDoorsCarTypeMap.put("B", "2 Doors");
    tempDoorsCarTypeMap.put("C", "4 Doors");
    tempDoorsCarTypeMap.put("D", "5 Doors");
    tempDoorsCarTypeMap.put("W", "Estate");
    tempDoorsCarTypeMap.put("T", "Convertible");
    tempDoorsCarTypeMap.put("F", "SUV");
    tempDoorsCarTypeMap.put("P", "Pick up");
    tempDoorsCarTypeMap.put("V", "Passenger Van");
    doorsCarTypeMap = Collections.unmodifiableMap(tempDoorsCarTypeMap);
  }

  private static final Map<String, String> transmissionMap;

  static {
    Map<String, String> tempTransmissionMap = new HashMap<>();
    tempTransmissionMap.put("M", "Manual");
    tempTransmissionMap.put("A", "Automatic");

    transmissionMap = Collections.unmodifiableMap(tempTransmissionMap);
  }

  private static final Map<String, String> fuelAirConMap;

  static {
    Map<String, String> tempFuelAirConMap = new HashMap<>();
    tempFuelAirConMap.put("N", "Petrol/No AC");
    tempFuelAirConMap.put("R", "Petrol/AC");

    fuelAirConMap = Collections.unmodifiableMap(tempFuelAirConMap);
  }

  public static void main(String[] args) {
    JSONParser jsonParser = new JSONParser();
    try {
      JSONObject object = (JSONObject) jsonParser.parse(new FileReader("vehicles.json"));
      JSONObject jsonObject = (JSONObject) object.get("Search");

      List priceSortedList = (List) jsonObject.get("VehicleList");
      List ratingSortedList = (List) jsonObject.get("VehicleList");

      URL url = new URL("http://www.databison.com/wp-content/uploads/2009/04/conversion-of-excel-in-fixed-width-text-file-format.png");
      HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
      httpURLConnection.setDoOutput(true);
      httpURLConnection.setRequestMethod("PUT");
//      InputStreamReader out = new InputStreamReader(
//              httpURLConnection.getInputStream());
      OutputStreamWriter out = new OutputStreamWriter(httpURLConnection.getOutputStream());
//      out.write("Resource content");
     /* BufferedReader bf = new BufferedReader(out);
      String line;
      StringBuilder sb = new StringBuilder();
      try {
        while ((line = bf.readLine()) != null) {
          sb.append(line).append("\n");
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
      System.out.println(sb.toString());*/

      Collections.sort(priceSortedList, new Comparator<JSONObject>() {
        @Override
        public int compare(JSONObject o1, JSONObject o2) {
          Double priceA = (Double) o1.get("price");
          Double priceB = (Double) o2.get("price");
          return priceA.compareTo(priceB);
        }
      });

      Collections.sort(ratingSortedList, new Comparator<JSONObject>() {
        @Override
        public int compare(JSONObject o1, JSONObject o2) {
          Double ratingA;
          Double ratingB;
          if (o1.get("rating") instanceof Long) {
            ratingA = ((Long) o1.get("rating")).doubleValue();
          } else
            ratingA = (Double) o1.get("rating");
          if (o2.get("rating") instanceof Long) {
            ratingB = ((Long) o2.get("rating")).doubleValue();
          } else
            ratingB = (Double) o2.get("rating");
          return -ratingA.compareTo(ratingB);
        }
      });
      // Part 1
      JSONArray vehiclePrice = new JSONArray();
      JSONArray sippJSONArray = new JSONArray();
      JSONArray ratingSortedArray = new JSONArray();
      for (int i = 0; i < priceSortedList.size(); i++) {
        JSONObject vehicle = (JSONObject) priceSortedList.get(i);
        JSONObject supplierCar = (JSONObject) ratingSortedList.get(i);

        System.out.println("{Vehicle name} – {Price}");
        System.out.println(vehicle.get("name") + " - " + vehicle.get("price"));
        JSONObject temp = new JSONObject();
        temp.put("Vehicle name",vehicle.get("name"));
        temp.put("Price",vehicle.get("price"));
        vehiclePrice.add(temp);

        JSONObject temp2 = new JSONObject();
        char[] sippCharArray = ((String) vehicle.get("sipp")).toCharArray();
        temp2.put("Vehicle name",vehicle.get("name"));
        temp2.put("SIPP",vehicle.get("sipp"));
        temp2.put("Car type",carTypeMap.get(Character.toString(sippCharArray[0])));
        temp2.put("Car type/doors",doorsCarTypeMap.get(Character.toString(sippCharArray[1])));
        temp2.put("Transmission",transmissionMap.get(Character.toString(sippCharArray[2])));
        temp2.put("Fuel",fuelAirConMap.get(Character.toString(sippCharArray[3])).split("/")[0]);
        temp2.put("Air con",fuelAirConMap.get(Character.toString(sippCharArray[3])).split("/")[1]);
        sippJSONArray.add(temp2);

        System.out.println("{Vehicle name} – {SIPP} – {Car type} – {Car type/doors} " +
                "– {Transmission} – {Fuel} – {Air con}");
        System.out.println(vehicle.get("name") + " - " + vehicle.get("sipp") + " - "
                + carTypeMap.get(Character.toString(sippCharArray[0])) + " - "
                + doorsCarTypeMap.get(Character.toString(sippCharArray[1])) + " - "
                + transmissionMap.get(Character.toString(sippCharArray[2])) + " - "
                + fuelAirConMap.get(Character.toString(sippCharArray[3])));


        System.out.println("{Vehicle name} – {Car type} – {Supplier} – {Rating}");
        sippCharArray = ((String) supplierCar.get("sipp")).toCharArray();
        System.out.println(supplierCar.get("name") + " - "
                + carTypeMap.get(Character.toString(sippCharArray[0])) + " - "
                + supplierCar.get("supplier") + " - "
                + supplierCar.get("rating"));
        JSONObject temp3 = new JSONObject();
        temp3.put("Vehicle name",supplierCar.get("name"));
        temp3.put("Car type",carTypeMap.get(Character.toString(sippCharArray[0])));
        temp3.put("Supplier",supplierCar.get("supplier"));
        temp3.put("Rating",supplierCar.get("rating"));
        ratingSortedArray.add(temp3);

        vehicle.put("score", giveCombinedScore(vehicle, sippCharArray[2], sippCharArray[3]));
        System.out.println("\n");
      }
      out.write(vehiclePrice.toJSONString());
      out.write(sippJSONArray.toJSONString());
      out.write(ratingSortedArray.toJSONString());

      ArrayList<JSONObject> scoreSortedList = new ArrayList<JSONObject>(priceSortedList);
      Collections.sort(scoreSortedList, new Comparator<JSONObject>() {
        @Override
        public int compare(JSONObject o1, JSONObject o2) {
          Double scoreA = (Double) o1.get("score");
          Double scoreB = (Double) o2.get("score");
          return -scoreA.compareTo(scoreB);
        }
      });
      JSONArray scoreSortedArray = new JSONArray();
      for (JSONObject car : scoreSortedList) {
        System.out.println("(Vehicle name} – {Vehicle score} – {Supplier rating} – {Sum of scores}");
        double carScore;
        if (car.get("rating") instanceof Long)
          carScore = (Double) car.get("score") - ((Long) car.get("rating")).doubleValue();
        else
          carScore = (Double) car.get("score") - (Double) car.get("rating");

        System.out.println(car.get("name") + " - " + carScore + " - " + car.get("rating") + " - " + car.get("score"));
        JSONObject temp = new JSONObject();
        temp.put("name",car.get("name"));
        temp.put("vehicle score",carScore);
        temp.put("rating",car.get("rating"));
        temp.put("Sum of scores",car.get("score"));
        scoreSortedArray.add(temp);
      }
      out.write(scoreSortedArray.toJSONString());
      out.close();
    } catch (IOException | ParseException ioException) {
      ioException.printStackTrace();
    }
  }

  private static double giveCombinedScore(JSONObject vehicle, char thirdChar, char fourthChar) {
    double score = 0;
    if (thirdChar == 'M') {
      score++;
    }
    if (thirdChar == 'A') {
      score = score + 5;
    }
    if (fourthChar == 'R') {
      score = score + 2;
    }
    if (vehicle.get("rating") instanceof Long)
      score = score + ((Long) vehicle.get("rating")).doubleValue();
    else
      score = score + (Double) vehicle.get("rating");
    return score;
  }
}
