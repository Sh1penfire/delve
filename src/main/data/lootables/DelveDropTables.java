package main.data.lootables;

import main.content.DelveItems;
import main.data.lootables.AttributeDropTableBase.AtributeDropTable;
import main.data.lootables.AttributeDropTableBase.TableDropEntry;
import main.items.ItemAttributes;

public class DelveDropTables {

    public static TableDropEntry
            oldstone = new TableDropEntry(){{
                prefs.add(new AttributeDropTableBase.AboveZeroPref(ItemAttributes.stone), new AttributeDropTableBase.AboveZeroPref(ItemAttributes.iron));

                entries.addAll(
                        new AttributeDropTableBase.ItemDropEntry(){{
                            type = DelveItems.oldstone;

                            prefs.add(new AttributeDropTableBase.MinCostPref(){{
                                cost.put(ItemAttributes.iron, 4).put( ItemAttributes.stone, 4);
                            }});
                            cost.put(ItemAttributes.iron, 0.25f).put(ItemAttributes.stone, 0.5f);
                            minCostScale = 3;
                            maxCostScale = 4;
                        }},
                        new AttributeDropTableBase.ItemDropEntry(){{

                            type = DelveItems.oldstoneDust;

                            cost.put(ItemAttributes.iron, 0.25f).put(ItemAttributes.stone, 0.5f);
                            minCostScale = 1;
                            maxCostScale = 3;
                        }}
                );
            }},

            geothite = new TableDropEntry(){{
                baseline = new ItemAttributes.AttributeData().put(ItemAttributes.iron, 10).put( ItemAttributes.stone, 10);

                prefs.add(new AttributeDropTableBase.MinCostPref(){{
                    cost.set(baseline);
                }});

                entries.addAll(
                        new AttributeDropTableBase.ItemDropEntry(){{
                            type = DelveItems.geothite;

                            prefs.add(new AttributeDropTableBase.MinCostPref(){{
                                cost.put(ItemAttributes.iron, 4).put( ItemAttributes.stone, 4);
                            }});
                            cost.put(ItemAttributes.iron, 1).put( ItemAttributes.stone, 1);
                            minCostScale = 3;
                            maxCostScale = 4;
                        }},
                        new AttributeDropTableBase.ItemDropEntry(){{

                            type = DelveItems.geothiteDust;

                            cost.put(ItemAttributes.iron, 1).put(ItemAttributes.stone, 1);
                            minCostScale = 1;
                            maxCostScale = 3;
                        }}
                );
            }},

            malachite = new TableDropEntry(){{
                baseline = new ItemAttributes.AttributeData().put(ItemAttributes.copper, 10).put( ItemAttributes.stone, 10);

                prefs.add(new AttributeDropTableBase.MinCostPref(){{
                    cost.set(baseline);
                }});

                entries.addAll(
                        new AttributeDropTableBase.ItemDropEntry(){{
                            type = DelveItems.malachite;

                            prefs.add(new AttributeDropTableBase.MinCostPref(){{
                                cost.put(ItemAttributes.copper, 4).put( ItemAttributes.stone, 4);
                            }});
                            cost.put(ItemAttributes.copper, 1).put( ItemAttributes.stone, 1);
                            minCostScale = 3;
                            maxCostScale = 4;
                        }},
                        new AttributeDropTableBase.ItemDropEntry(){{

                            type = DelveItems.malachiteDust;

                            cost.put(ItemAttributes.iron, 1).put(ItemAttributes.stone, 1);
                            minCostScale = 1;
                            maxCostScale = 3;
                        }}
                );
            }},

            stone = new TableDropEntry(){{
                prefs.add(new AttributeDropTableBase.AboveZeroPref(ItemAttributes.stone));

                entries.addAll(
                        new AttributeDropTableBase.ItemDropEntry(){{
                            type = DelveItems.stone;

                            prefs.add(new AttributeDropTableBase.MinCostPref(){{
                                cost.put( ItemAttributes.stone, 8);
                            }});
                            cost.put( ItemAttributes.stone, 1);
                            minCostScale = 8;
                            maxCostScale = 12;
                        }},
                        new AttributeDropTableBase.ItemDropEntry(){{
                            type = DelveItems.stone;

                            prefs.add(new AttributeDropTableBase.MinCostPref(){{
                                cost.put( ItemAttributes.stone, 4);
                            }});
                            cost.put(ItemAttributes.stone, 1);
                            minCostScale = 4;
                            maxCostScale = 5;
                        }},
                        new AttributeDropTableBase.ItemDropEntry(){{
                            type = DelveItems.stoneDust;

                            cost.put(ItemAttributes.stone, 1);
                            minCostScale = 1;
                            maxCostScale = 3;
                        }}
                );
            }};

    public static TableDropEntry rust = new TableDropEntry(){{
        prefs.add(new AttributeDropTableBase.AboveZeroPref(ItemAttributes.iron));

        entries.addAll(
                new AttributeDropTableBase.ItemDropEntry(){{
                    type = DelveItems.rust;

                    prefs.add(new AttributeDropTableBase.MinCostPref(){{
                        cost.put(ItemAttributes.iron, 4);
                    }});
                    cost.put(ItemAttributes.iron, 1);
                    minCostScale = 3;
                    maxCostScale = 4;
                }},
                new AttributeDropTableBase.ItemDropEntry(){{

                    type = DelveItems.rustDust;

                    cost.put(ItemAttributes.iron, 1);
                    minCostScale = 1;
                    maxCostScale = 3;
                }}
        );
    }};
    public static TableDropEntry sekosDust = new TableDropEntry(){{
        prefs.add(new AttributeDropTableBase.AboveZeroPref(ItemAttributes.copper));

        entries.addAll(
                new AttributeDropTableBase.ItemDropEntry(){{

                    type = DelveItems.sekos;

                    cost.put(ItemAttributes.copper, 1);
                    minCostScale = 1;
                    maxCostScale = 3;
                }}
        );
    }};

    public static AtributeDropTable defaultDroptable = new AtributeDropTable(){{
        mainEntry = new TableDropEntry(){{

            entries.addAll(
                    malachite,
                    geothite,
                    stone,
                    rust,
                    sekosDust
            );
        }};
    }},

    oldstoneDroptable = new AtributeDropTable(){{
        mainEntry = oldstone;
    }};
}
