import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;


public class RentalCarsApplication {

    private static final String UNIFORM_RESOURCE_LOCATOR =
            "http://www.databison.com/wp-content/uploads/2009/04/conversion-of-excel-in-fixed-width-text-file-format.png";
    private static final int FUEL = 0;
    private static final int AIR_CON = 1;
    private static final int FIRST_LETTER = 0;
    private static final int SECOND_LETTER = 1;
    private static final int THIRD_LETTER = 2;
    private static final int FOURTH_LETTER = 3;

    public static void main(String[] args) {
        JSONParser jsonParser = new JSONParser();

        final Map<String, String> fuelAirConMap = createFuelAirConMap();
        final Map<String, String> transmissionMap = createTransmissionMap();
        final Map<String, String> doorsCarTypeMap = creatDoorsCarTypeMap();
        final Map<String, String> carTypeMap = createCarTypeMap();

        try {
            JSONObject object = (JSONObject) jsonParser.parse(new FileReader("vehicles.json"));
            JSONObject jsonObject = (JSONObject) object.get("Search");

            List priceSortedList = (List) jsonObject.get("VehicleList");
            List ratingSortedList = (List) jsonObject.get("VehicleList");

            // Establish connection
            URL url = new URL(UNIFORM_RESOURCE_LOCATOR);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("PUT");
            OutputStreamWriter out = new OutputStreamWriter(httpURLConnection.getOutputStream());

            // Sort by price / ascending
            Collections.sort(priceSortedList, new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject o1, JSONObject o2) {
                    Double priceA = (Double) o1.get("price");
                    Double priceB = (Double) o2.get("price");
                    return priceA.compareTo(priceB);
                }
            });

            // Sort by rating / descending
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
            // Create array for each output format.
            JSONArray vehiclePrice = new JSONArray();
            JSONArray sippJSONArray = new JSONArray();
            JSONArray ratingSortedArray = new JSONArray();

            for (int i = 0; i < priceSortedList.size(); i++) {
                JSONObject vehicle = (JSONObject) priceSortedList.get(i);
                JSONObject supplierCar = (JSONObject) ratingSortedList.get(i);

                printAscendingPriceOrder(vehiclePrice, vehicle);

                // Store vehicle specification
                JSONObject tempJsonObj = new JSONObject();
                char[] sippCharArray = ((String) vehicle.get("sipp")).toCharArray();

                String fourthLetter = Character.toString(sippCharArray[FOURTH_LETTER]);
                String thirdLetter = Character.toString(sippCharArray[THIRD_LETTER]);
                String secondLetter = Character.toString(sippCharArray[SECOND_LETTER]);
                String firstLetter = Character.toString(sippCharArray[FIRST_LETTER]);

                String fuel = fuelAirConMap.get(fourthLetter).split("/")[FUEL];
                String airCon = fuelAirConMap.get(fourthLetter).split("/")[AIR_CON];
                tempJsonObj.put("Vehicle name", vehicle.get("name"));
                tempJsonObj.put("SIPP", vehicle.get("sipp"));
                tempJsonObj.put("Car type", carTypeMap.get(firstLetter));
                tempJsonObj.put("Car type/doors", doorsCarTypeMap.get(secondLetter));
                tempJsonObj.put("Transmission", transmissionMap.get(thirdLetter));
                tempJsonObj.put("Fuel", fuel);
                tempJsonObj.put("Air con", airCon);
                sippJSONArray.add(tempJsonObj);

                // Print vehicle specification
                System.out.println("{Vehicle name} – {SIPP} – {Car type} – {Car type/doors} " +
                        "– {Transmission} – {Fuel} – {Air con}");
                System.out.println(vehicle.get("name") + " - " + vehicle.get("sipp") + " - "
                        + carTypeMap.get(firstLetter) + " - "
                        + doorsCarTypeMap.get(secondLetter) + " - "
                        + transmissionMap.get(thirdLetter) + " - "
                        + fuel + " - "
                        + airCon);

                // Print highest rated supplier per car type / descending
                System.out.println("{Vehicle name} – {Car type} – {Supplier} – {Rating}");
                sippCharArray = ((String) supplierCar.get("sipp")).toCharArray();
                String ratingFirstLetter = Character.toString(sippCharArray[FIRST_LETTER]);

                System.out.println(supplierCar.get("name") + " - "
                        + carTypeMap.get(ratingFirstLetter) + " - "
                        + supplierCar.get("supplier") + " - "
                        + supplierCar.get("rating"));
                JSONObject tempObj = new JSONObject();
                tempObj.put("Vehicle name", supplierCar.get("name"));
                tempObj.put("Car type", carTypeMap.get(Character.toString(sippCharArray[0])));
                tempObj.put("Supplier", supplierCar.get("supplier"));
                tempObj.put("Rating", supplierCar.get("rating"));
                ratingSortedArray.add(tempObj);

                vehicle.put("score", giveCombinedScore(vehicle, sippCharArray[THIRD_LETTER],
                        sippCharArray[FOURTH_LETTER]));
                System.out.println("\n");
            }
            out.write(vehiclePrice.toJSONString());
            out.write(sippJSONArray.toJSONString());
            out.write(ratingSortedArray.toJSONString());

            // Sort by the sum of the scores in descending order
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
                temp.put("name", car.get("name"));
                temp.put("vehicle score", carScore);
                temp.put("rating", car.get("rating"));
                temp.put("Sum of scores", car.get("score"));
                scoreSortedArray.add(temp);
            }

            out.write(scoreSortedArray.toJSONString());
            out.close();
        } catch (IOException | ParseException ioException) {
            ioException.printStackTrace();
        }
    }

    private static void printAscendingPriceOrder(JSONArray vehiclePrice, JSONObject vehicle) {
        System.out.println("{Vehicle name} – {Price}");
        System.out.println(vehicle.get("name") + " - " + vehicle.get("price"));
        JSONObject tempJsonObj = new JSONObject();
        tempJsonObj.put("Vehicle name", vehicle.get("name"));
        tempJsonObj.put("Price", vehicle.get("price"));
        vehiclePrice.add(tempJsonObj);
    }

    private static Map<String, String> createCarTypeMap() {
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
        return Collections.unmodifiableMap(tempCarTypeMap);
    }

    private static Map<String, String> creatDoorsCarTypeMap() {
        Map<String, String> tempDoorsCarTypeMap = new HashMap<>();
        tempDoorsCarTypeMap.put("B", "2 Doors");
        tempDoorsCarTypeMap.put("C", "4 Doors");
        tempDoorsCarTypeMap.put("D", "5 Doors");
        tempDoorsCarTypeMap.put("W", "Estate");
        tempDoorsCarTypeMap.put("T", "Convertible");
        tempDoorsCarTypeMap.put("F", "SUV");
        tempDoorsCarTypeMap.put("P", "Pick up");
        tempDoorsCarTypeMap.put("V", "Passenger Van");
        return Collections.unmodifiableMap(tempDoorsCarTypeMap);
    }

    private static Map<String, String> createTransmissionMap() {
        Map<String, String> tempTransmissionMap = new HashMap<>();
        tempTransmissionMap.put("M", "Manual");
        tempTransmissionMap.put("A", "Automatic");

        return Collections.unmodifiableMap(tempTransmissionMap);
    }

    private static Map<String, String> createFuelAirConMap() {
        final Map<String, String> fuelAirConMap = new HashMap<>();
        fuelAirConMap.put("N", "Petrol/No AC");
        fuelAirConMap.put("R", "Petrol/AC");
        return Collections.unmodifiableMap(fuelAirConMap);
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
