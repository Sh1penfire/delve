package main.data.lootables;

import arc.math.Mathf;
import arc.math.Rand;
import arc.struct.Seq;
import arc.util.Log;
import main.items.ItemAttributes;
import main.items.ItemConstructors;
import main.items.ItemState;
import mindustry.Vars;
import mindustry.type.Item;


/**
 * A token-based loot table system build to use item attributes :D
 */
public abstract class AttributeDropTableBase {

    public static ItemAttributes.AttributeData attrs = new ItemAttributes.AttributeData();

    //Set the seed of the attribute drop table's rand when querying for deterministic drops!
    public static Rand rand = new Rand();

    public abstract void get(Seq<ItemState> items, ItemAttributes.AttributeData data, int maxRolls);

    public static class AtributeDropTable extends AttributeDropTableBase{

        public DropEntry mainEntry;

        @Override
        public void get(Seq<ItemState> items, ItemAttributes.AttributeData data, int maxRolls) {
            attrs.set(data);
            boolean valid = true;
            int i = 0;

            while(valid){
                i++;
                valid = mainEntry.valid(items, attrs) && mainEntry.get(items, attrs) && i < maxRolls;
            }
        }
    }

    public abstract class DropEntry{

        //Weight of this pool. Used for random chosing.
        public int weight;

        public Seq<CostPref> prefs = new Seq<>();

        //Returns true if this entry can be used
        public boolean valid(Seq<ItemState> items, ItemAttributes.AttributeData data){
            for(CostPref pref: prefs){
                if(!pref.valid(items, data)) return false;
            }
            return true;
        };

        //Returns true if adding items
        public abstract boolean get(Seq<ItemState> items, ItemAttributes.AttributeData data);
    }

    private static boolean foundInvalid;


    //Checks its entries in order of index
    public class TableDropEntry extends DropEntry{

        public Seq<DropEntry> entries = new Seq<>();

        public ItemAttributes.AttributeData baseline = new ItemAttributes.AttributeData();

        @Override
        public boolean get(Seq<ItemState> items, ItemAttributes.AttributeData curAttributes) {
            curAttributes.addData(baseline, -1);
            boolean addedItem = false;

            for(DropEntry entry: entries){
                if(entry.valid(items, curAttributes) && entry.get(items, curAttributes)) {
                    addedItem = true;
                    break;
                }
            }
            curAttributes.addData(baseline, 1);

            return addedItem;
        }
    }

    //Creates item states
    public class ItemDropEntry extends DropEntry{

        //The cost for this item entry
        public ItemAttributes.AttributeData cost = new ItemAttributes.AttributeData();

        public Item type;

        //The cost range for this item entry
        public int minCostScale, maxCostScale;

        //The base properties for the item
        public ItemConstructors.ItemData properties = new ItemConstructors.ItemData();

        float maxScaling;
        @Override
        public boolean get(Seq<ItemState> items, ItemAttributes.AttributeData curAttributes) {
            maxScaling = maxCostScale;
            cost.each((attrId, amount) -> {
                maxScaling = Math.min(curAttributes.getValue(attrId, 1)/amount, maxScaling);
            });

            if(maxScaling < minCostScale) return false;

            int scale = Mathf.floor(rand.random(minCostScale, maxScaling + 1));

            if(scale <= 0) return false;

            //Remove stuff
            curAttributes.addData(cost, -scale);
            items.add(ItemConstructors.getEntry(type.name).get(Vars.world, properties, scale));

            return true;
        }
    }


    public abstract class CostPref{
        public abstract boolean valid(Seq<ItemState> items, ItemAttributes.AttributeData data);
    }

    //Check if the data is above a certain required cost to activate
    public class MinCostPref extends CostPref{

        public MinCostPref(){
        }

        public MinCostPref(ItemAttributes.AttributeData data){
            this.cost = data;
        }

        //The cost for this item entry
        public ItemAttributes.AttributeData cost = new ItemAttributes.AttributeData();

        @Override
        public boolean valid(Seq<ItemState> items, ItemAttributes.AttributeData curAttributes) {
            foundInvalid = false;

            cost.each((attrId, amount) -> {

                if(foundInvalid || curAttributes.getValue(attrId, 0) < amount) {
                    foundInvalid = true;
                }
            });
            return !foundInvalid;
        }
    }
    //Check if the data is above a certain required cost to activate
    public class AboveZeroPref extends CostPref{


        public AboveZeroPref(ItemAttributes.ItemAttribute attribute){
            this.attribute = attribute;
        }

        //The cost for this item entry
        public ItemAttributes.ItemAttribute attribute;

        @Override
        public boolean valid(Seq<ItemState> items, ItemAttributes.AttributeData curAttributes) {
            return curAttributes.get(attribute, 0) > 0;
        }
    }
}
