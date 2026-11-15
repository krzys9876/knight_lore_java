package org.kr.kl.objects;

public class BackgroundObjects {
    private final BackgroundObject[] data = new BackgroundObject[] {
            new BackgroundObject("arch_n_6D12"),
            new BackgroundObject("arch_e_6D23"),
            new BackgroundObject("arch_s_6D45"),
            new BackgroundObject("arch_w_6D67"),
            new BackgroundObject("tree_arch_n_6D78"),
            new BackgroundObject("tree_arch_e_6D89"),
            new BackgroundObject("tree_arch_s_6D9A"),
            new BackgroundObject("tree_arch_w_6DAB"),
            new BackgroundObject("gate_n_6DBC"),
            new BackgroundObject("gate_e_6DC5"),
            new BackgroundObject("gate_s_6DCE"),
            new BackgroundObject("gate_w_6DD7"),
            new BackgroundObject("wall_size_1_6DE0"),
            new BackgroundObject("wall_size_2_6E49"),
            new BackgroundObject("wall_size_3_6EBA"),
            new BackgroundObject("tree_room_size_1_6F2B"),
            new BackgroundObject("tree_filler_w_6F8C"),
            new BackgroundObject("tree_filler_n_6F9D"),
            new BackgroundObject("wizard_6FAE"),
            new BackgroundObject("cauldron_6FBF"),
            new BackgroundObject("high_arch_e_6D34"),
            new BackgroundObject("high_arch_s_6D56"),
            new BackgroundObject("high_arch_e_base_6FD0"),
            new BackgroundObject("high_arch_s_base_6FE1")
    };

    public BackgroundObject get(int i) { return data[i];}
}
