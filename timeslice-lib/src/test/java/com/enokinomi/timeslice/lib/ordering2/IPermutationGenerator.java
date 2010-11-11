package com.enokinomi.timeslice.lib.ordering2;

import java.util.List;
import java.util.Set;

interface IPermutationGenerator
{
    Set<List<String>> generatePermutations(List<String> prefix, Set<String> leftovers);
}