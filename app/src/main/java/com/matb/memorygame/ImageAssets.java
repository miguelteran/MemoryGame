package com.matb.memorygame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ImageAssets
{
    private static final List<Integer> baseImages = new ArrayList<Integer>()
    {{
        add(R.drawable.aerodynamic_20concrete_20clock);
        add(R.drawable.aerodynamic_20cotton_20keyboard);
        add(R.drawable.aerodynamic_20granite_20plate);
        add(R.drawable.aerodynamic_20linen_20computer);
        add(R.drawable.aerodynamic_20rubber_20shoes);
        add(R.drawable.aerodynamic_20wooden_20table);
        add(R.drawable.awesome_20bronze_20computer);
        add(R.drawable.awesome_20concrete_20watch);
        add(R.drawable.awesome_20copper_20chair);
        add(R.drawable.awesome_20cotton_20computer);
        add(R.drawable.awesome_20cotton_20table);
        add(R.drawable.awesome_20silk_20pants);
        add(R.drawable.awesome_20bronze_20bag);
        add(R.drawable.durable_20aluminum_20knife);
        add(R.drawable.durable_20aluminum_20watch);
        add(R.drawable.durable_20concrete_20lamp);
        add(R.drawable.durable_20paper_20pants);
        add(R.drawable.durable_20rubber_20clock);
        add(R.drawable.enormous_20concrete_20chair);
        add(R.drawable.enormous_20copper_20coat);
        add(R.drawable.enormous_20cotton_20hat);
        add(R.drawable.enormous_20iron_20wallet);
        add(R.drawable.enormous_20leather_20lamp);
        add(R.drawable.enormous_20marble_20hat);
        add(R.drawable.enormous_20marble_20plate);
        add(R.drawable.enormous_20steel_20lamp);
        add(R.drawable.enormous_20wooden_20car);
        add(R.drawable.ergonomic_20concrete_20clock);
        add(R.drawable.ergonomic_20concrete_20knife);
        add(R.drawable.ergonomic_20concrete_20wallet);
        add(R.drawable.ergonomic_20copper_20computer);
        add(R.drawable.ergonomic_20cotton_20computer);
        add(R.drawable.ergonomic_20iron_20wallet);
        add(R.drawable.ergonomic_20leather_20lamp);
        add(R.drawable.ergonomic_20paper_20computer);
        add(R.drawable.ergonomic_20plastic_20bench);
        add(R.drawable.ergonomic_20plastic_20clock);
        add(R.drawable.ergonomic_20rubber_20plate);
        add(R.drawable.ergonomic_20wooden_20pants);
        add(R.drawable.ergonomic_20wool_20plate);
        add(R.drawable.fantastic_20leather_20wallet);
        add(R.drawable.fantastic_20wool_20bench);
        add(R.drawable.fantastic_20wool_20bottle);
        add(R.drawable.gorgeous_20aluminum_20watch);
        add(R.drawable.gorgeous_20concrete_20plate);
        add(R.drawable.gorgeous_20cotton_20gloves);
        add(R.drawable.gorgeous_20linen_20clock);
        add(R.drawable.gorgeous_20paper_20coat);
        add(R.drawable.gorgeous_20paper_20hat);
    }};

    public static List<Integer> getImages(int numberOfGroups)
    {
        int endIndex = numberOfGroups > baseImages.size() ? baseImages.size() : numberOfGroups;
        List<Integer> subList = baseImages.subList(0, endIndex);

        List<Integer> images = new ArrayList<>(subList);
        images.addAll(subList);
        Collections.shuffle(images);
        
        return images;
    }
}
