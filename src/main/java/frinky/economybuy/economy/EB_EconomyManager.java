package frinky.economybuy.economy;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import frinky.economybuy.EB_Blocks;
import frinky.economybuy.EB_Cash_Interface;
import frinky.economybuy.EB_Items;
import frinky.economybuy.EB_Util;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class EB_EconomyManager {
    private static EB_EconomyManager instance;
    public final List<EB_ShopItem> shopItems  = new ArrayList<>();
    public final Map<Integer, Item> moneyValues = new HashMap<>();




    private EB_EconomyManager() {
        // Private constructor to prevent instantiation

        moneyValues.put(1, EB_Items.CASH);
        moneyValues.put(EB_Items.WAD_OF_CASH.getCashValue(new ItemStack(EB_Items.WAD_OF_CASH)), EB_Items.WAD_OF_CASH);
        moneyValues.put(EB_Items.STACK_OF_CASH.getCashValue(new ItemStack(EB_Items.STACK_OF_CASH)), EB_Items.STACK_OF_CASH);
        moneyValues.put(((EB_Cash_Interface) EB_Blocks.BLOCK_OF_CASH.asItem()).getCashValue(new ItemStack(EB_Blocks.BLOCK_OF_CASH.asItem())), EB_Blocks.BLOCK_OF_CASH.asItem());

    }

    public static EB_EconomyManager get() {
        if (instance == null) {
            instance = new EB_EconomyManager();
        }
        return instance;
    }

    public static void initialize() {
        get();
    }

    public void syncMarket(MinecraftServer server) {
        String API_URL = "https://script.google.com/macros/s/AKfycbxBWq5ALmyL_2z7utHklZNWeW6Q3ED8ke25xIw5eUS7VQLCGK4MyxRXnR0ohRcpuUHb/exec";

        server.getPlayerManager().broadcast(Text.literal("Syncing Market"), false);

        CompletableFuture.runAsync(() -> {
            try {
                URL url = URL.of(URI.create(API_URL), null);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");


                // Read the response body
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    Gson gson = new Gson();
                    List<Map<String, Object>> marketData = gson.fromJson(response.toString(), new TypeToken<List<Map<String, Object>>>(){}.getType());

                    shopItems.clear(); // hopefully by the time we're here, we have a valid list of shop items, or else we're fucked, but ohwell, you can only hope

                    for (Map<String, Object> item : marketData) {
                        try {
                            String name = (String) item.get("name");
                            System.out.println("Processing item: " + name);

                            double buyPriceRaw = (Double) item.get("buyPrice");
                            double sellPriceRaw = (Double) item.get("sellPrice");
                            int buyPrice = (int) buyPriceRaw;
                            int sellPrice = (int) sellPriceRaw;
                            String categoryStr = (String) item.get("category");

                            System.out.println("Values: buyPrice=" + buyPrice + ", sellPrice=" + sellPrice + ", category=" + categoryStr);

                            if (name == null || name.isBlank() || buyPrice < 0 || sellPrice < 0) {
                                System.out.println("Skipping invalid item: " + name);
                                continue;
                            }

                            // Ensure name has correct namespace
                            if (!name.contains(":")) {
                                name = "minecraft:" + name;
                            }

                            if (!Registries.ITEM.containsId(Identifier.of(name))) {
                                System.out.println("Invalid item identifier: " + name);
                                continue;
                            }

                            Item minecraftItem = EB_Util.GetItemByName(name);
                            EB_ShopItem.Category category = EB_ShopItem.Category.valueOf(categoryStr);

                            EB_ShopItem shopItem = new EB_ShopItem(minecraftItem, buyPrice, sellPrice, category);
                            shopItems.add(shopItem);
                            System.out.println("Successfully added item: " + name);

                        } catch (Exception e) {
                            System.out.println("Error processing item: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
                // Schedule the broadcast back on the main thread
                server.execute(() ->
                        server.getPlayerManager().broadcast(Text.literal("Sync Completed"), false)
                );
            } catch (IOException e) {
                server.execute(() ->
                        server.getPlayerManager().broadcast(Text.literal("Sync Failed: " + e.getMessage()), false)
                );
            }
        });
    }
}
