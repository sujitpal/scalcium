package com.mycompany.scalcium.utils

import java.io.File

import org.junit.Assert
import org.junit.Test

import net.didion.jwnl.data.POS

class WordnetTest {

  val wn = new Wordnet(new File("src/main/resources/wnconfig.xml"))

  /**
   * >>> from nltk.corpus import wordnet as wn
   * >>> wn.synsets('motorcar')
   * [Synset('car.n.01')]
   */
  @Test
  def testSynsets(): Unit = {
    Console.println(">>> wn.synsets('motorcar')")
    val motorcar = wn.synsets("motorcar")
    Console.println(motorcar.map(m => wn.format(m)))
    Assert.assertNotNull(motorcar)
  }
  
  /**
   * >>> wn.synset('car.n.01').lemma_names
   * ['car', 'auto', 'automobile', 'machine', 'motorcar']
   */
  @Test
  def testSynsetLemmaNames(): Unit = {
    Console.println(">>> wn.synset('car.n.01').lemma_names")
    val lms = wn.lemmaNames(wn.synset("car", POS.NOUN, 1))
    Console.println(lms)
    Assert.assertEquals(5, lms.size)
  }
  
  /**
   * >>> wn.synset('car.n.01').definition
   * 'a motor vehicle with four wheels; usually propelled by \
   * an internal combustion engine'
   */
  @Test
  def testSynsetDefinition(): Unit = {
    Console.println(">>> wn.synset('car.n.01').definition")
    val sd = wn.definition(wn.synset("car", POS.NOUN, 1))
    Console.println(sd)
    Assert.assertTrue(sd.contains(";"))
  }
  
  /**
   * >>> wn.synset('car.n.01').examples
   * ['he needs a car to get to work']
   */
  @Test
  def testSynsetExamples(): Unit = {
    Console.println(">>> wn.synset('car.n.01').examples")
    val se = wn.examples(wn.synset("car", POS.NOUN, 1))
    Console.println(se)
    Assert.assertEquals(1, se.size)
  }
  
  /**
   * >>> wn.synset('car.n.01').lemmas
   * [Lemma('car.n.01.car'), Lemma('car.n.01.auto'), \
   * Lemma('car.n.01.automobile'),\
   * Lemma('car.n.01.machine'), Lemma('car.n.01.motorcar')]
   * >>> wn.synset('car.n.01').lemmas[1]
   * Lemma('car.n.01.auto')
   */
  @Test
  def testSynsetLemmas(): Unit = {
    Console.println(">>> wn.synset('car.n.01').lemmas")
    val sl = wn.lemmas(wn.synset("car", POS.NOUN, 1))
    Console.println(sl.map(l => wn.format(l)))
    Assert.assertEquals(5, sl.size)
    Assert.assertTrue(sl(1).getLemma().equals("auto"))
  }
  
  /**
   * >>> wn.lemma('car.n.01.automobile').name
   * 'automobile'
   */
  @Test
  def testSynsetLemma(): Unit = {
    Console.println(">>> wn.lemma('car.n.01.automobile').name")
    val sl = wn.lemma(wn.synset("car", POS.NOUN, 1), 2)
    sl match {
      case Some(x) => {
        Console.println(x.getLemma())
        Assert.assertTrue("automobile".equals(x.getLemma()))
      }
      case None => Assert.fail()
    }
  }
  
  /**
   * >>> for synset in wn.synsets('car'):
   * ...     print synset.lemma_names
   * ...
   * ['car', 'auto', 'automobile', 'machine', 'motorcar']
   * ['car', 'railcar', 'railway_car', 'railroad_car']
   * ['car', 'gondola']
   * ['car', 'elevator_car']
   * ['cable_car', 'car']
   */
  @Test
  def testSynsetsAndLemmaNames(): Unit = {
    Console.println(">>> for synset in wn.synsets('car'):")
    Console.println("...     print synset.lemma_names")
    Console.println("...")
    val lns = wn.synsets("car")
      .map(ss => wn.lemmaNames(Some(ss)))
    lns.foreach(ln => 
      Console.println("[" + ln.mkString(", ") + "]"))
    Assert.assertEquals(5, lns.size)
    Assert.assertEquals(5, lns(0).size)
  }
  
  /**
   * >>> wn.lemmas('car')
   * [Lemma('car.n.01.car'), Lemma('car.n.02.car'), \
   * Lemma('car.n.03.car'), Lemma('car.n.04.car'), \
   * Lemma('cable_car.n.01.car')]
   * :NOTE: in NLTK, the third field in Lemma indicates 
   * the (unique) sequence number of the synset from which
   * the lemma is derived. For example, Lemma('car.n.01.car')
   * comes from the first synset with word(0) == "car".
   * JWNL does not capture the information, the index
   * here means the sequence number of the lemma inside
   * the synset.
   */
  @Test
  def testLemmas(): Unit = {
    Console.println(">>> wn.lemmas('car')")
    val ls = wn.lemmas("car")
    Console.println(ls.map(l => wn.format(l)))
  }
  
  /**
   * >>> motorcar = wn.synset('car.n.01')
   * >>> types_of_motorcar = motorcar.hyponyms()
   * >>> types_of_motorcar[26]
   * Synset('ambulance.n.01')
   * :NOTE: NLTK's wordnet returns hyponyms in a different
   * order than JWNL but both return the same number of
   * synsets. Test is modified accordingly.
   */
  @Test
  def testHyponyms(): Unit = {
    Console.println(">>> motorcar = wn.synset('car.n.01')")
    Console.println(">>> types_of_motorcar = motorcar.hyponyms()")
    val motorcar = wn.synset("car", POS.NOUN, 1)
    val typesOfMotorcar = wn.hyponyms(motorcar)
    Console.println(">>> types_of_motorcar")
    Console.println(typesOfMotorcar.map(ss => wn.format(ss)))
    Assert.assertEquals(31, typesOfMotorcar.size)
    Console.println(">>> types_of_motorcar[0]")
    val ambulance = typesOfMotorcar(0)
    Console.println(wn.format(ambulance))
    Assert.assertEquals("ambulance.n.01", wn.format(ambulance))
    Console.println(">>> sorted([lemma.name for synset\n" +  
       "...    in types_of_motorcar for lemma in synset.lemmas])")
    val sortedMotorcarNames = typesOfMotorcar
      .map(ss => wn.lemmaNames(Some(ss))(0))
      .sortWith((a,b) => a < b)
    Console.println(sortedMotorcarNames)
    Assert.assertEquals("Model_T", sortedMotorcarNames(0))
  }
  
  /**
   * >>> motorcar.hypernyms()
   * [Synset('motor_vehicle.n.01')]
   */
  @Test
  def testHypernyms(): Unit = {
    Console.println(">> motorcar.hypernyms")
    val motorcar = wn.synset("car", POS.NOUN, 1)
    val parents = wn.hypernyms(motorcar)
    Console.println(parents.map(p => wn.format(p)))
    Assert.assertEquals(1, parents.size)
    Assert.assertEquals("motor_vehicle.n.01", 
      wn.format(parents(0)))
  }
  
  /**
   * >>> paths = motorcar.hypernym_paths()
   * >>> len(paths)
   * 2
   * >>> [synset.name for synset in paths[0]]
   * ['entity.n.01', 'physical_entity.n.01', 'object.n.01', 
   * 'whole.n.02', 'artifact.n.01', 'instrumentality.n.03', 
   * 'container.n.01', 'wheeled_vehicle.n.01',
   * 'self-propelled_vehicle.n.01', 'motor_vehicle.n.01', 
   * 'car.n.01']
   * >>> [synset.name for synset in paths[1]]
   * ['entity.n.01', 'physical_entity.n.01', 'object.n.01', 
   * 'whole.n.02', 'artifact.n.01', 'instrumentality.n.03', 
   * 'conveyance.n.03', 'vehicle.n.01', 
   * 'wheeled_vehicle.n.01', 'self-propelled_vehicle.n.01', 
   * 'motor_vehicle.n.01', 'car.n.01']
   * >>> motorcar.root_hypernyms()
   * [Synset('entity.n.01')]
   */
  @Test
  def testHypernymPaths(): Unit = {
    Console.println(">>> paths = motorcar.hypernym_paths()")
    Console.println(">>> len(paths)")
    val motorcar = wn.synset("car", POS.NOUN, 1)
    val paths = wn.hypernymPaths(motorcar)
    Console.println(paths.size)
    Assert.assertEquals(2, paths.size)
    Console.println(">>> [synset.name for synset in paths[0]]")
    val paths0 = paths(0).map(ss => wn.format(ss))
    Console.println(paths0)
    Console.println(">>> [synset.name for synset in paths[1]]")
    val paths1 = paths(1).map(ss => wn.format(ss))
    Console.println(paths1)
    Console.println(">>> motorcar.root_hypernyms()")
    val rhns = wn.rootHypernyms(motorcar)
      .map(rhn => wn.format(rhn))
    Console.println(rhns)
  }
  
  /**
   * >>> wn.synset('tree.n.01').part_meronyms()
   * [Synset('burl.n.02'), Synset('crown.n.07'), 
   * Synset('stump.n.01'), Synset('trunk.n.01'), 
   * Synset('limb.n.02')]
   * >>> wn.synset('tree.n.01').substance_meronyms()
   * [Synset('heartwood.n.01'), Synset('sapwood.n.01')]
   * >>> wn.synset('tree.n.01').member_holonyms()
   * [Synset('forest.n.01')]
   */
  @Test
  def testMiscRelationMethods(): Unit = {
    Console.println(">>> wn.synset('tree.n.01').part_meronyms()")
    val tree = wn.synset("tree", POS.NOUN, 1)
    val pn = wn.partMeronyms(tree)
    Console.println(pn.map(ss => wn.format(ss)))
    Assert.assertEquals(5, pn.size)
    Console.println(">>> wn.synset('tree.n.01').substance_meronyms()")
    val sn = wn.substanceMeronyms(tree)
    Assert.assertEquals(2, sn.size)
    Console.println(sn.map(ss => wn.format(ss)))
    Console.println(">>> wn.synset('tree.n.01').member_holonyms()")
    val mn = wn.memberHolonyms(tree)
    Assert.assertEquals(1, mn.size)
    Console.println(mn.map(ss => wn.format(ss)))
  }
  
  /**
   * >>> for synset in wn.synsets('mint', wn.NOUN):
   * ...     print synset.name + ':', synset.definition
   * ...
   * batch.n.02: (often followed by `of') a large number or amount or extent
   * mint.n.02: any north temperate plant of the genus Mentha with aromatic leaves and
   *        small mauve flowers
   * mint.n.03: any member of the mint family of plants
   * mint.n.04: the leaves of a mint plant used fresh or candied
   * mint.n.05: a candy that is flavored with a mint oil
   * mint.n.06: a plant where money is coined by authority of the government
   * >>> wn.synset('mint.n.04').part_holonyms()
   * [Synset('mint.n.02')]
   * >>> [x.definition for x 
   * ...    in wn.synset('mint.n.04').part_holonyms()]
   * ['any north temperate plant of the genus Mentha with 
   *   aromatic leaves and small mauve flowers']
   * >>> wn.synset('mint.n.04').substance_holonyms()
   * [Synset('mint.n.05')]
   * >>> [x.definition for x 
   * ...    in wn.synset('mint.n.04').substance_holonyms()]
   * ['a candy that is flavored with a mint oil']
   */
  @Test
  def testListSynsetNameDefinition(): Unit = {
    val mintss = wn.synsets("mint", POS.NOUN)
    Assert.assertEquals(6, mintss.size)
    Console.println(">>> for synset in wn.synsets('mint', wn.NOUN):")
    Console.println("...     print synset.name + ':', synset.definition")
    Console.println("...")
    mintss.foreach(ss => 
      Console.println(wn.format(ss) + ": " + 
        wn.definition(Some(ss))))
    Console.println(">>> wn.synset('mint.n.04').part_holonyms()")
    val mint = wn.synset("mint", POS.NOUN, 4)
    val ph = wn.partHolonyms(mint)
    Console.println(ph.map(ss => wn.format(ss)))
    Console.println(">>> [x.definition for x") 
    Console.println("...    in wn.synset('mint.n.04').part_holonyms()]")
    Console.println(ph.map(ss => wn.definition(Some(ss))))
    Console.println(">>> wn.synset('mint.n.04').substance_holonyms()")
    val sh = wn.substanceHolonyms(mint)
    Console.println(sh.map(ss => wn.format(ss)))
    Console.println(">>> [x.definition for x") 
    Console.println("...    in wn.synset('mint.n.04').substance_holonyms()]")
    Console.println(sh.map(ss => wn.definition(Some(ss))))
  }
  
  /**
   * >>> wn.synset('walk.v.01').entailments()
   * [Synset('step.v.01')]
   * >>> wn.synset('eat.v.01').entailments()
   * [Synset('swallow.v.01'), Synset('chew.v.01')]
   * >>> wn.synset('tease.v.03').entailments()
   * [Synset('arouse.v.07'), Synset('disappoint.v.01')]
   */
  @Test
  def testVerbRelationships(): Unit = {
    Console.println(">>> wn.synset('walk.v.01').entailments()")
    val walk = wn.synset("walk", POS.VERB, 1)
    val walkEnt = wn.entailments(walk)
    Console.println(walkEnt.map(ss => wn.format(ss)))
    Assert.assertEquals(1, walkEnt.size)
    Console.println(">>> wn.synset('eat.v.01').entailments()")
    val eat = wn.synset("eat", POS.VERB, 1)
    val eatEnt = wn.entailments(eat)
    Console.println(eatEnt.map(ss => wn.format(ss)))
    Assert.assertEquals(2, eatEnt.size)
    Console.println(">>> wn.synset('tease.v.03').entailments()")
    val tease = wn.synset("tease", POS.VERB, 3)
    val teaseEnt = wn.entailments(tease)
    Console.println(teaseEnt.map(ss => wn.format(ss)))
    Assert.assertEquals(2, teaseEnt.size)
  }
  
  /**
   * >>> wn.lemma('supply.n.02.supply').antonyms()
   * [Lemma('demand.n.02.demand')]
   * >>> wn.lemma('rush.v.01.rush').antonyms()
   * [Lemma('linger.v.04.linger')]
   * >>> wn.lemma('horizontal.a.01.horizontal').antonyms()
   * [Lemma('vertical.a.01.vertical'), 
   * Lemma('inclined.a.02.inclined')]
   * >>> wn.lemma('staccato.r.01.staccato').antonyms()
   * [Lemma('legato.r.01.legato')]
   */
  @Test
  def testLemmaAntonyms(): Unit = {
    Console.println(">>> wn.lemma('supply.n.02.supply').antonyms()")
    val supply = wn.lemma(wn.synset("supply", POS.NOUN, 2), "supply")
    val supplyAntonyms = wn.antonyms(supply)
    Console.println(supplyAntonyms.map(w => wn.format(w)))
    Assert.assertEquals(1, supplyAntonyms.size)
    Console.println(">>> wn.lemma('rush.v.01.rush').antonyms()")
    val rush = wn.lemma(wn.synset("rush", POS.VERB, 1), "rush")
    val rushAntonyms = wn.antonyms(rush)
    Console.println(rushAntonyms.map(w => wn.format(w)))
    Assert.assertEquals(1, rushAntonyms.size)
    Console.println(">>> wn.lemma('horizontal.a.01.horizontal').antonyms()")
    val horizontal = wn.lemma(wn.synset("horizontal", POS.ADJECTIVE, 1), "horizontal")
    val horizontalAntonyms = wn.antonyms(horizontal)
    Console.println(horizontalAntonyms.map(w => wn.format(w)))
    Assert.assertEquals(2, horizontalAntonyms.size)
    Console.println(">>> wn.lemma('staccato.r.01.staccato').antonyms()")
    val staccato = wn.lemma(wn.synset("staccato", POS.ADVERB, 1), "staccato")
    val staccatoAntonyms = wn.antonyms(staccato)
    Console.println(staccatoAntonyms.map(w => wn.format(w)))
    Assert.assertEquals(1, staccatoAntonyms.size)
  }

  /**
   * >>> right = wn.synset('right_whale.n.01')
   * >>> orca = wn.synset('orca.n.01')
   * >>> minke = wn.synset('minke_whale.n.01')
   * >>> tortoise = wn.synset('tortoise.n.01')
   * >>> novel = wn.synset('novel.n.01')
   * >>> right.lowest_common_hypernyms(minke)
   * [Synset('baleen_whale.n.01')]
   * >>> right.lowest_common_hypernyms(orca)
   * [Synset('whale.n.02')]
   * >>> right.lowest_common_hypernyms(tortoise)
   * [Synset('vertebrate.n.01')]
   * >>> right.lowest_common_hypernyms(novel)
   * [Synset('entity.n.01')]
   */
  @Test
  def testSynsetLowestCommonHypernyms(): Unit = {
    Console.println(">>> right = wn.synset('right_whale.n.01')")
    Console.println(">>> orca = wn.synset('orca.n.01')")
    Console.println(">>> minke = wn.synset('minke_whale.n.01')")
    Console.println(">>> tortoise = wn.synset('tortoise.n.01')")
    Console.println(">>> novel = wn.synset('novel.n.01')")
    val right = wn.synset("right_whale", POS.NOUN, 1)
    val orca = wn.synset("orca", POS.NOUN, 1)
    val minke = wn.synset("minke_whale", POS.NOUN, 1)
    val tortoise = wn.synset("tortoise", POS.NOUN, 1)
    val novel = wn.synset("novel", POS.NOUN, 1)
    Console.println(">>> right.lowest_common_hypernyms(minke)")
    val rightMinkeLCH = wn.lowestCommonHypernym(right, minke)
    Console.println(rightMinkeLCH.map(ss => wn.format(ss)))
    Console.println(">>> right.lowest_common_hypernyms(orca)")
    val rightOrcaLCH = wn.lowestCommonHypernym(right, orca)
    Console.println(rightOrcaLCH.map(ss => wn.format(ss)))
    Console.println(">>> right.lowest_common_hypernyms(tortoise)")
    val rightTortoiseLCH = wn.lowestCommonHypernym(right, tortoise)
    Console.println(rightTortoiseLCH.map(ss => wn.format(ss)))
    Console.println(">>> right.lowest_common_hypernyms(novel)")
    val rightNovelLCH = wn.lowestCommonHypernym(right, novel)
    Console.println(rightNovelLCH.map(ss => wn.format(ss)))
  }
  
  /**
   * >>> wn.synset('baleen_whale.n.01').min_depth()
   * 14
   * >>> wn.synset('whale.n.02').min_depth()
   * 13
   * >>> wn.synset('vertebrate.n.01').min_depth()
   * 8
   * >>> wn.synset('entity.n.01').min_depth()
   * 0
   */
  @Test
  def testSynsetMinDepth(): Unit = {
    Console.println(">>> wn.synset('baleen_whale.n.01').min_depth()")
    val baleenWhaleMinDepth = wn.minDepth(wn.synset("baleen_whale", POS.NOUN, 1))
    Console.println(baleenWhaleMinDepth)
    Assert.assertEquals(14, baleenWhaleMinDepth)
    Console.println(">>> wn.synset('whale.n.02').min_depth()")
    val whaleMinDepth = wn.minDepth(wn.synset("whale", POS.NOUN, 2))
    Console.println(whaleMinDepth)
    Assert.assertEquals(13, whaleMinDepth)
    Console.println(">>> wn.synset('vertebrate.n.01').min_depth()")
    val vertebrateMinDepth = wn.minDepth(wn.synset("vertebrate", POS.NOUN, 1))
    Console.println(vertebrateMinDepth)
    Assert.assertEquals(8, vertebrateMinDepth)
    Console.println(">>> wn.synset('entity.n.01').min_depth()")
    val entityMinDepth = wn.minDepth(wn.synset("entity", POS.NOUN, 1))
    Console.println(entityMinDepth)
    Assert.assertEquals(0, entityMinDepth)
  }
  
  /**
   * >>> right.path_similarity(minke)
   * 0.25
   * >>> right.path_similarity(orca)
   * 0.16666666666666666
   * >>> right.path_similarity(tortoise)
   * 0.076923076923076927
   * >>> right.path_similarity(novel)
   * 0.043478260869565216
   */
  @Test
  def testPathSimilarity(): Unit = {
    val right = wn.synset("right_whale", POS.NOUN, 1)
    val orca = wn.synset("orca", POS.NOUN, 1)
    val minke = wn.synset("minke_whale", POS.NOUN, 1)
    val tortoise = wn.synset("tortoise", POS.NOUN, 1)
    val novel = wn.synset("novel", POS.NOUN, 1)
    Console.println(">>> right.path_similarity(minke)")
    val rightMinkePathSimilarity = wn.pathSimilarity(right, minke) 
    Console.println(rightMinkePathSimilarity)
    Assert.assertEquals(0.25D, rightMinkePathSimilarity, 0.01D)
    Console.println(">>> right.path_similarity(orca)")
    val rightOrcaPathSimilarity = wn.pathSimilarity(right, orca) 
    Console.println(rightOrcaPathSimilarity)
    Assert.assertEquals(0.1667D, rightOrcaPathSimilarity, 0.01D)
    Console.println(">>> right.path_similarity(tortoise)")
    val rightTortoisePathSimilarity = wn.pathSimilarity(right, tortoise) 
    Console.println(rightTortoisePathSimilarity)
    Assert.assertEquals(0.0769D, rightTortoisePathSimilarity, 0.01D)
    Console.println(">>> right.path_similarity(novel)")
    val rightNovelPathSimilarity = wn.pathSimilarity(right, novel) 
    Console.println(rightNovelPathSimilarity)
    Assert.assertEquals(0.043D, rightNovelPathSimilarity, 0.01D)
  }

  /**
   * >>> dog = wn.synset('dog.n.01')
   * >>> cat = wn.synset('cat.n.01')
   * >>> hit = wn.synset('hit.v.01')
   * >>> slap = wn.synset('slap.v.01')
   * >>> dog.path_similarity(cat)
   * 0.2...
   * >>> hit.path_similarity(slap)
   * 0.142...
   * >>> dog.lch_similarity(cat)
   * 2.028...
   * >>> hit.lch_similarity(slap)
   * 1.312...
   * >>> dog.wup_similarity(cat)
   * 0.857...
   * >>> hit.wup_similarity(slap)
   * 0.25
   * >>> dog.res_similarity(cat, semcor_ic)
   * 7.911...
   * >>> dog.jcn_similarity(cat, semcor_ic)
   * 0.449...
   * >>> dog.lin_similarity(cat, semcor_ic)
   * 0.886...
   */
  @Test
  def testOtherSimilarities(): Unit = {
    Console.println(">>> dog = wn.synset('dog.n.01')")
    Console.println(">>> cat = wn.synset('cat.n.01')")
    Console.println(">>> hit = wn.synset('hit.v.01')")
    Console.println(">>> slap = wn.synset('slap.v.01')")
    val dog = wn.synset("dog", POS.NOUN, 1)
    val cat = wn.synset("cat", POS.NOUN, 1)
    val hit = wn.synset("hit", POS.VERB, 1)
    val slap = wn.synset("slap", POS.VERB, 1)
    
    Console.println(">>> dog.path_similarity(cat)")
    val dogCatPathSimilarity = wn.pathSimilarity(dog, cat) 
    Console.println(dogCatPathSimilarity)
    Console.println(">>> hit.path_similarity(slap)")
    val hitSlapPathSimilarity = wn.pathSimilarity(hit, slap)
    Console.println(hitSlapPathSimilarity)
    Assert.assertEquals(0.2D, dogCatPathSimilarity, 0.01D)
    Assert.assertEquals(0.1428D, hitSlapPathSimilarity, 0.01D)
    
    Console.println(">>> dog.lch_similarity(cat)")
    val dogCatLchSimilarity = wn.lchSimilarity(dog, cat)
    Console.println(dogCatLchSimilarity)
    Console.println(">>> hit.lch_similarity(slap)")
    val hitSlapLchSimilarity = wn.lchSimilarity(hit, slap)
    Console.println(hitSlapLchSimilarity)
    Assert.assertEquals(2.079D, dogCatLchSimilarity, 0.01D)
    Assert.assertEquals(1.386D, hitSlapLchSimilarity, 0.01D)
    
    Console.println(">>> dog.wup_similarity(cat)")
    val dogCatWupSimilarity = wn.wupSimilarity(dog, cat)
    Console.println(dogCatWupSimilarity)
    Console.println(">>> hit.wup_similarity(slap)")
    val hitSlapWupSimilarity = wn.wupSimilarity(hit, slap)
    Console.println(hitSlapWupSimilarity)
    Assert.assertEquals(0.866D, dogCatWupSimilarity, 0.01D)
    Assert.assertEquals(0.25D, hitSlapWupSimilarity, 0.01D)
    
    Console.println(">>> dog.res_similarity(cat)")
    val dogCatResSimilarity = wn.resSimilarity(dog, cat)
    Console.println(dogCatResSimilarity)
    Console.println(">>> hit.res_similarity(slap)")
    Assert.assertEquals(7.254D, dogCatResSimilarity, 0.01D)

    Console.println(">>> dog.jcn_similarity(cat)")
    val dogCatJcnSimilarity = wn.jcnSimilarity(dog, cat)
    Console.println(dogCatJcnSimilarity)
    Assert.assertEquals(0.537D, dogCatJcnSimilarity, 0.01D)

    Console.println(">>> dog.lin_similarity(cat)")
    val dogCatLinSimilarity = wn.linSimilarity(dog, cat)
    Console.println(dogCatLinSimilarity)
    Assert.assertEquals(0.886D, dogCatLinSimilarity, 0.01D)
  }
  
  /**
   * >>> for synset in list(wn.all_synsets('n'))[:10]:
   * ...     print(synset)
   * ...
   * Synset('entity.n.01')
   * Synset('physical_entity.n.01')
   * Synset('abstraction.n.06')
   * Synset('thing.n.12')
   * Synset('object.n.01')
   * Synset('whole.n.02')
   * Synset('congener.n.03')
   * Synset('living_thing.n.01')
   * Synset('organism.n.01')
   * Synset('benthos.n.02')
   * :NOTE: order of synsets returned is different in 
   * JWNL than with NLTK.
   */
  @Test
  def testAllSynsets(): Unit = {
    Console.println(">>> for synset in list(wn.all_synsets('n'))[:10]:")
    Console.println("...     print(synset)")
    Console.println("...")
    val fss = wn.allSynsets(POS.NOUN)
      .take(10)
      .toList
    fss.foreach(ss => Console.println(wn.format(ss)))
    Assert.assertEquals(10, fss.size)
  }
  
  /**
   * >>> print(wn.morphy('denied', wn.VERB))
   * deny
   * >>> print(wn.morphy('dogs'))
   * dog
   * >>> print(wn.morphy('churches'))
   * church
   * >>> print(wn.morphy('aardwolves'))
   * aardwolf
   * >>> print(wn.morphy('abaci'))
   * abacus
   * >>> print(wn.morphy('book', wn.NOUN))
   * book
   * >>> wn.morphy('hardrock', wn.ADV)
   * >>> wn.morphy('book', wn.ADJ)
   * >>> wn.morphy('his', wn.NOUN)
   */
  @Test
  def testMorphy(): Unit = {
    Console.println(">>> print(wn.morphy('denied', wn.VERB))")
    val denied = wn.morphy("denied", POS.VERB)
    Console.println(denied)
    Assert.assertEquals("deny", denied)
    Console.println(">>> print(wn.morphy('dogs'))")
    val dogs = wn.morphy("dogs")
    Console.println(dogs)
    Assert.assertEquals("dog", dogs)
    Console.println(">>> print(wn.morphy('churches'))")
    val churches = wn.morphy("churches")
    Console.println(churches)
    Assert.assertEquals("church", churches)
    Console.println(">>> print(wn.morphy('aardwolves'))")
    val aardwolves = wn.morphy("aardwolves")
    Console.println(aardwolves)
    Assert.assertEquals("aardwolf", aardwolves)
    Console.println(">>> print(wn.morphy('abaci'))")
    val abaci = wn.morphy("abaci")
    Console.println(abaci)
    Assert.assertEquals("abacus", abaci)
    Console.println(">>> print(wn.morphy('book', wn.NOUN))")
    val book = wn.morphy("book", POS.NOUN)
    Console.println(book)
    Assert.assertEquals("book", book)
    Console.println(">>> wn.morphy('hardrock', wn.ADV)")
    val hardrock = wn.morphy("hardrock", POS.ADVERB)
    Console.println(hardrock)
    Assert.assertTrue(hardrock.isEmpty)
    Console.println(">>> wn.morphy('book', wn.ADJ)")
    val bookAdj = wn.morphy("book", POS.ADJECTIVE)
    Console.println(bookAdj)
    Assert.assertTrue(bookAdj.isEmpty)
    Console.println(">>> wn.morphy('his', wn.NOUN)")
    val his = wn.morphy("his", POS.NOUN)
    Console.println(his)
    Assert.assertTrue(his.isEmpty)
  }
}
