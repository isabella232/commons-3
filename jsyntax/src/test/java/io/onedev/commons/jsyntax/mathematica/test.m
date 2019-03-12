Changex[GeList_List, Pc_Integer, {f_Function, Kind_String}] := Block[
  {GeCopy = GeList},
  Switch[(*���Ƕ��ֱ仯ѡ��*)
   Kind,
   "Change", GeCopy[[Pc]] = f[GeCopy[[Pc]]],
   "Add", GeCopy[[Pc]] += f[GeCopy[[Pc]]]]; GeCopy
  ]

GeChange[p_Real, GeList_List, n_Integer, {f_Function, Kind_String}] :=
 (*ͻ����ʣ��������У�ͻ��n�Σ���ͻ�亯����ͻ��ģʽ��*)
 Block[
  {Len = Length[GeList],
   Pc = 0,
   GeCopy = 0
   },
  If[RandomReal[] < p,
   GeCopy = GeList;
   Nest[Changex[#, RandomInteger[{1, Len}], {f, Kind}] &, GeCopy, n](*n�ε���*)
   , GeList]
  ]

GeChangeList[GeList_List, pList_List, {f_Function, Kind_String}] :=
 (*�������У�ͻ�����У���ͻ�亯����ͻ��ģʽ��*)
 Block[
  {Len = Length[GeList],
   Pc = 0,
   GeCopy = GeList
   },
  Do[GeCopy = Changex[GeCopy, pList[[i]], {f, Kind}];, {i, 1, Length[pList]}];
  GeCopy
  ]

GePMC[GeList_List, {S_Integer, T_Integer}] :=(*����������*)
 (*��Ҫͻ��Ļ��򣬣���ʼλ������λ��*)
 Module[{GeCopy = GeList,
   head = GeList[[1 ;; S - 1]],
   body = GeList[[S ;; T]],
   foot = GeList[[T + 1 ;; Length[GeList]]]
   },
  head~Join~Reverse[body]~Join~foot
  ]