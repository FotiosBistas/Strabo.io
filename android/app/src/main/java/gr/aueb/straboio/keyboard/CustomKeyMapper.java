package gr.aueb.straboio.keyboard;

import java.util.Map;

public class CustomKeyMapper {
    private final Map<Integer, Triplet<Character, Character, Character>> CODE_TO_LANG = Map.ofEntries(

            // digits
            Map.entry(49, new Triplet<>('1', '1', '$')),
            Map.entry(50, new Triplet<>('2', '2', '$')),
            Map.entry(51, new Triplet<>('3', '3', '$')),
            Map.entry(52, new Triplet<>('4', '4', '$')),
            Map.entry(53, new Triplet<>('5', '5', '$')),
            Map.entry(54, new Triplet<>('6', '6', '$')),
            Map.entry(55, new Triplet<>('7', '7', '$')),
            Map.entry(56, new Triplet<>('8', '8', '$')),
            Map.entry(57, new Triplet<>('9', '9', '$')),
            Map.entry(48, new Triplet<>('0', '0', '$')),

            // modifier keys
            Map.entry(44, new Triplet<>(',', ',', '$')),
            Map.entry(32, new Triplet<>(' ', ' ', '$')),
            Map.entry(46, new Triplet<>('.', '.', '$')),

            // first row
            Map.entry(113, new Triplet<>('q', ';', '$')),
            Map.entry(81, new Triplet<>('Q', ':', '$')),

            Map.entry(119, new Triplet<>('w', 'ς', '$')),
            Map.entry(87, new Triplet<>('W', 'ς', '$')),

            Map.entry(101, new Triplet<>('e', 'ε', 'έ')),
            Map.entry(69, new Triplet<>('E', 'Ε', 'Έ')),

            Map.entry(114, new Triplet<>('r', 'ρ', '$')),
            Map.entry(82, new Triplet<>('R', 'Ρ', '$')),

            Map.entry(116, new Triplet<>('t', 'τ', '$')),
            Map.entry(84, new Triplet<>('T', 'Τ', '$')),

            Map.entry(121, new Triplet<>('y', 'υ', 'ύ')),
            Map.entry(89, new Triplet<>('Y', 'Υ', 'Ύ')),

            Map.entry(117, new Triplet<>('u', 'θ', '$')),
            Map.entry(85, new Triplet<>('U', 'Θ', '$')),

            Map.entry(105, new Triplet<>('i', 'ι', 'ί')),
            Map.entry(73, new Triplet<>('I', 'Ι', 'Ί')),

            Map.entry(111, new Triplet<>('o', 'ο', 'ό')),
            Map.entry(79, new Triplet<>('O', 'Ο', 'Ό')),

            Map.entry(112, new Triplet<>('p', 'π', '$')),
            Map.entry(80, new Triplet<>('P', 'Π', '$')),

            // sencond row
            Map.entry(97, new Triplet<>('a', 'α', 'ά')),
            Map.entry(65, new Triplet<>('A', 'Α', 'Ά')),

            Map.entry(115, new Triplet<>('s', 'σ', '$')),
            Map.entry(83, new Triplet<>('S', 'Σ', '$')),

            Map.entry(100, new Triplet<>('d', 'δ', '$')),
            Map.entry(68, new Triplet<>('D', 'Δ', '$')),

            Map.entry(102, new Triplet<>('f', 'φ', '$')),
            Map.entry(70, new Triplet<>('F', 'Φ', '$')),

            Map.entry(103, new Triplet<>('g', 'γ', '$')),
            Map.entry(71, new Triplet<>('G', 'Γ', '$')),

            Map.entry(104, new Triplet<>('h', 'η', 'ή')),
            Map.entry(72, new Triplet<>('H', 'Η', 'Ή')),

            Map.entry(106, new Triplet<>('j', 'ξ', '$')),
            Map.entry(74, new Triplet<>('J', 'Ξ', '$')),

            Map.entry(107, new Triplet<>('k', 'κ', '$')),
            Map.entry(75, new Triplet<>('K', 'Κ', '$')),

            Map.entry(108, new Triplet<>('l', 'λ', '$')),
            Map.entry(76, new Triplet<>('L', 'Λ', '$')),

            //third row
            Map.entry(122, new Triplet<>('z', 'ζ', '$')),
            Map.entry(90, new Triplet<>('Z', 'Ζ', '$')),

            Map.entry(120, new Triplet<>('x', 'χ', '$')),
            Map.entry(88, new Triplet<>('X', 'Χ', '$')),

            Map.entry(99, new Triplet<>('c', 'ψ', '$')),
            Map.entry(67, new Triplet<>('C', 'Ψ', '$')),

            Map.entry(118, new Triplet<>('v', 'ω', 'ώ')),
            Map.entry(86, new Triplet<>('V', 'Ω', 'Ώ')),

            Map.entry(98, new Triplet<>('b', 'β', '$')),
            Map.entry(66, new Triplet<>('B', 'Β', '$')),

            Map.entry(110, new Triplet<>('n', 'ν', '$')),
            Map.entry(78, new Triplet<>('N', 'Ν', '$')),

            Map.entry(109, new Triplet<>('m', 'μ', '$')),
            Map.entry(77, new Triplet<>('M', 'Μ', '$'))

    );

    public char toEN(int code){
        Triplet tri = CODE_TO_LANG.get(code);
        return tri != null ? (char) tri.getFirst() : null;

    }

    public char toGR(int code){
        Triplet tri = CODE_TO_LANG.get(code);
        return tri != null ? (char) tri.getSecond() : null;
    }

    public char toSPECIAL(int code){
        Triplet tri = CODE_TO_LANG.get(code);
        return tri != null ? (char) tri.getThird() : null;
    }

}
