export type PhonemeCategory =
  | 'short-vowel'
  | 'long-vowel'
  | 'diphthong'
  | 'voiced-consonant'
  | 'unvoiced-consonant'
  | 'other-consonant';

export interface Phoneme {
  symbol: string;           // IPA symbol like "θ"
  name: string;             // "voiceless dental fricative"
  category: PhonemeCategory;
  keywords: string[];       // ["think", "bath", "truth"]
  description: string;      // How to physically produce the sound
  spanishTip: string;       // Common mistake for Spanish speakers
}

export const CATEGORY_LABELS: Record<PhonemeCategory, string> = {
  'short-vowel': 'Short Vowels',
  'long-vowel': 'Long Vowels',
  'diphthong': 'Diphthongs',
  'voiced-consonant': 'Voiced Consonants',
  'unvoiced-consonant': 'Unvoiced Consonants',
  'other-consonant': 'Other Consonants',
};

export const CATEGORY_ORDER: PhonemeCategory[] = [
  'short-vowel',
  'long-vowel',
  'diphthong',
  'voiced-consonant',
  'unvoiced-consonant',
  'other-consonant',
];

export const PHONEMES: Phoneme[] = [
  // ==================== SHORT VOWELS ====================
  {
    symbol: 'ɪ',
    name: 'near-close near-front unrounded vowel',
    category: 'short-vowel',
    keywords: ['sit', 'hit', 'women'],
    description: 'Tongue high and forward, but lower than /iː/. Lips relaxed and slightly spread. Short duration.',
    spanishTip: 'No existe en español. Es más corta y relajada que la "i" española. No digas "seet" sino "sit" con sonido breve.',
  },
  {
    symbol: 'ɛ',
    name: 'open-mid front unrounded vowel',
    category: 'short-vowel',
    keywords: ['bed', 'said', 'many'],
    description: 'Tongue mid-height, forward position. Jaw slightly more open than /ɪ/. Lips neutral.',
    spanishTip: 'Similar a la "e" española pero más abierta. La mandíbula baja un poco más que en español.',
  },
  {
    symbol: 'æ',
    name: 'near-open front unrounded vowel',
    category: 'short-vowel',
    keywords: ['cat', 'bad', 'hand'],
    description: 'Tongue low and forward. Jaw wide open. Lips spread. Sound between "a" and "e".',
    spanishTip: 'No existe en español. Abre la boca como para decir "a" pero intenta decir "e". No confundir con la "a" española.',
  },
  {
    symbol: 'ʌ',
    name: 'open-mid back unrounded vowel',
    category: 'short-vowel',
    keywords: ['cup', 'love', 'blood'],
    description: 'Tongue mid-height, central to slightly back. Lips neutral and relaxed. Short and unstressed.',
    spanishTip: 'Parecida a una "a" corta y relajada. No pronunciar como "o". "Cup" no es "cop".',
  },
  {
    symbol: 'ɒ',
    name: 'open back rounded vowel',
    category: 'short-vowel',
    keywords: ['hot', 'box', 'wash'],
    description: 'Tongue low and back. Lips rounded. Jaw wide open. British accent primarily.',
    spanishTip: 'Entre la "a" y la "o" española, con labios redondeados. En inglés americano suena más como /ɑː/.',
  },
  {
    symbol: 'ʊ',
    name: 'near-close near-back rounded vowel',
    category: 'short-vowel',
    keywords: ['book', 'put', 'could'],
    description: 'Tongue high and back, but lower than /uː/. Lips rounded but relaxed. Short duration.',
    spanishTip: 'Más corta y relajada que la "u" española. No digas "boot" cuando quieres decir "book".',
  },
  {
    symbol: 'ə',
    name: 'mid central vowel (schwa)',
    category: 'short-vowel',
    keywords: ['about', 'sofa', 'zebra'],
    description: 'The most common English sound. Tongue completely relaxed in neutral position. Very short.',
    spanishTip: 'El sonido más importante del inglés. Todas las vocales átonas se reducen a esta "e" neutra y relajada.',
  },

  // ==================== LONG VOWELS ====================
  {
    symbol: 'iː',
    name: 'close front unrounded vowel',
    category: 'long-vowel',
    keywords: ['sheep', 'see', 'piece'],
    description: 'Tongue high and forward. Lips spread in a smile. Hold the sound longer than Spanish "i".',
    spanishTip: 'Similar a la "i" española pero MÁS LARGA. "Ship" /ɪ/ vs "sheep" /iː/ - la diferencia está en la duración.',
  },
  {
    symbol: 'ɑː',
    name: 'open back unrounded vowel',
    category: 'long-vowel',
    keywords: ['father', 'car', 'start'],
    description: 'Tongue low and back. Jaw wide open. Lips neutral. Long duration.',
    spanishTip: 'Como la "a" española pero MÁS LARGA y con la lengua un poco más atrás. Mantén el sonido.',
  },
  {
    symbol: 'ɔː',
    name: 'open-mid back rounded vowel',
    category: 'long-vowel',
    keywords: ['door', 'saw', 'caught'],
    description: 'Tongue mid-back. Lips strongly rounded. Long duration.',
    spanishTip: 'Similar a la "o" española pero más larga y cerrada. Redondea bien los labios y sostén el sonido.',
  },
  {
    symbol: 'uː',
    name: 'close back rounded vowel',
    category: 'long-vowel',
    keywords: ['food', 'blue', 'move'],
    description: 'Tongue high and back. Lips tightly rounded. Long duration.',
    spanishTip: 'Como la "u" española pero MÁS LARGA. "Full" /ʊ/ es corta, "fool" /uː/ es larga.',
  },
  {
    symbol: 'ɜː',
    name: 'open-mid central unrounded vowel',
    category: 'long-vowel',
    keywords: ['bird', 'learn', 'turn'],
    description: 'Tongue mid-central. Lips slightly rounded. Long duration. Often with r-coloring in American English.',
    spanishTip: 'No existe en español. Es como una "e" larga pronunciada con la lengua enrollada (en inglés americano).',
  },

  // ==================== DIPHTHONGS ====================
  {
    symbol: 'eɪ',
    name: 'close-mid front to close front diphthong',
    category: 'diphthong',
    keywords: ['day', 'make', 'eight'],
    description: 'Start with /ɛ/, glide smoothly to /ɪ/. Two vowels blended as one syllable.',
    spanishTip: 'No es la "e" española pura. Empieza como "e" y termina deslizando hacia "i". "Say" no es "se".',
  },
  {
    symbol: 'aɪ',
    name: 'open front to close front diphthong',
    category: 'diphthong',
    keywords: ['my', 'high', 'ride'],
    description: 'Start with /a/, glide to /ɪ/. Jaw opens wide then closes.',
    spanishTip: 'Similar al "ai" en "aire" pero empieza con sonido más abierto. Desliza suavemente entre las dos vocales.',
  },
  {
    symbol: 'ɔɪ',
    name: 'open-mid back to close front diphthong',
    category: 'diphthong',
    keywords: ['boy', 'coin', 'voice'],
    description: 'Start with rounded /ɔ/, glide to /ɪ/. Lips move from rounded to spread.',
    spanishTip: 'Como "oi" en "boina". Empieza con "o" redondeada y desliza hacia "i".',
  },
  {
    symbol: 'aʊ',
    name: 'open front to close back diphthong',
    category: 'diphthong',
    keywords: ['now', 'house', 'loud'],
    description: 'Start with /a/, glide to /ʊ/. Lips move from neutral to rounded.',
    spanishTip: 'Como "au" en "pausa". Abre la boca con "a" y cierra redondeando hacia "u".',
  },
  {
    symbol: 'əʊ',
    name: 'mid central to close back diphthong',
    category: 'diphthong',
    keywords: ['go', 'home', 'show'],
    description: 'Start with schwa /ə/, glide to /ʊ/. British primarily; Americans use /oʊ/.',
    spanishTip: 'NO es la "o" española pura. Empieza relajada (schwa) y desliza hacia "u". "Go" tiene dos sonidos.',
  },
  {
    symbol: 'ɪə',
    name: 'near-close front to mid central diphthong',
    category: 'diphthong',
    keywords: ['here', 'beer', 'dear'],
    description: 'Start with /ɪ/, glide to schwa /ə/. Common in British English.',
    spanishTip: 'Empieza con "i" corta y relaja hacia schwa. En inglés americano a veces se reduce a /ɪr/.',
  },
  {
    symbol: 'eə',
    name: 'close-mid front to mid central diphthong',
    category: 'diphthong',
    keywords: ['air', 'care', 'bear'],
    description: 'Start with /ɛ/, glide to schwa /ə/. Common in British English.',
    spanishTip: 'Empieza con "e" abierta y relaja hacia schwa. En inglés americano suena como /ɛr/.',
  },
  {
    symbol: 'ʊə',
    name: 'near-close back to mid central diphthong',
    category: 'diphthong',
    keywords: ['poor', 'tour', 'sure'],
    description: 'Start with /ʊ/, glide to schwa /ə/. Less common, often simplified.',
    spanishTip: 'Empieza con "u" corta y relaja hacia schwa. Muchos hablantes lo simplifican a /ɔː/.',
  },

  // ==================== VOICED CONSONANTS ====================
  {
    symbol: 'b',
    name: 'voiced bilabial plosive',
    category: 'voiced-consonant',
    keywords: ['big', 'rub', 'baby'],
    description: 'Both lips pressed together, then released with voice. Vocal cords vibrate.',
    spanishTip: 'Similar al español pero más explosiva al inicio de palabra. Vibran las cuerdas vocales.',
  },
  {
    symbol: 'd',
    name: 'voiced alveolar plosive',
    category: 'voiced-consonant',
    keywords: ['dog', 'red', 'made'],
    description: 'Tongue tip touches alveolar ridge (behind upper teeth), then releases with voice.',
    spanishTip: 'Similar al español pero la lengua toca más arriba (alveolos, no dientes). Más explosiva.',
  },
  {
    symbol: 'g',
    name: 'voiced velar plosive',
    category: 'voiced-consonant',
    keywords: ['go', 'big', 'egg'],
    description: 'Back of tongue touches soft palate, then releases with voice.',
    spanishTip: 'Como la "g" en "gato". Asegúrate de que vibren las cuerdas vocales.',
  },
  {
    symbol: 'v',
    name: 'voiced labiodental fricative',
    category: 'voiced-consonant',
    keywords: ['very', 'love', 'have'],
    description: 'Upper teeth touch lower lip lightly. Air flows through with voice.',
    spanishTip: 'NO EXISTE en español. No es "b". Los dientes superiores tocan el labio inferior. Vibra la garganta.',
  },
  {
    symbol: 'ð',
    name: 'voiced dental fricative',
    category: 'voiced-consonant',
    keywords: ['this', 'mother', 'breathe'],
    description: 'Tongue tip between teeth. Air flows through with voice. Continuous sound.',
    spanishTip: 'NO EXISTE en español. La lengua sale entre los dientes. Vibran las cuerdas vocales. No es "d".',
  },
  {
    symbol: 'z',
    name: 'voiced alveolar fricative',
    category: 'voiced-consonant',
    keywords: ['zoo', 'buzz', 'is'],
    description: 'Tongue near alveolar ridge. Air flows through narrow gap with voice. Like buzzing.',
    spanishTip: 'Como el zumbido de una abeja. NO es la "s" española. La garganta vibra. "Zip" vs "sip".',
  },
  {
    symbol: 'ʒ',
    name: 'voiced postalveolar fricative',
    category: 'voiced-consonant',
    keywords: ['vision', 'measure', 'beige'],
    description: 'Tongue behind alveolar ridge. Lips slightly rounded. Air flows with voice.',
    spanishTip: 'Como la "y" argentina en "yo" pero con vibración. O la "j" francesa en "je".',
  },
  {
    symbol: 'dʒ',
    name: 'voiced postalveolar affricate',
    category: 'voiced-consonant',
    keywords: ['job', 'edge', 'large'],
    description: 'Start like /d/, then release into /ʒ/. One single sound combining stop and fricative.',
    spanishTip: 'Como "ll" o "y" argentina pero más explosiva. "Jump" no es "yump". Es un sonido único.',
  },
  {
    symbol: 'm',
    name: 'voiced bilabial nasal',
    category: 'voiced-consonant',
    keywords: ['man', 'summer', 'him'],
    description: 'Lips closed. Air flows through nose. Vocal cords vibrate.',
    spanishTip: 'Igual que en español. Labios cerrados, aire por la nariz.',
  },
  {
    symbol: 'n',
    name: 'voiced alveolar nasal',
    category: 'voiced-consonant',
    keywords: ['no', 'win', 'sunny'],
    description: 'Tongue tip on alveolar ridge. Air flows through nose. Vocal cords vibrate.',
    spanishTip: 'Similar al español pero la lengua toca más arriba (alveolos, no dientes).',
  },
  {
    symbol: 'ŋ',
    name: 'voiced velar nasal',
    category: 'voiced-consonant',
    keywords: ['sing', 'wrong', 'think'],
    description: 'Back of tongue touches soft palate. Air flows through nose. Never followed by /g/ sound.',
    spanishTip: 'Como la "n" en "banco". NO digas "sing-guh". Es solo el sonido nasal, sin "g" al final.',
  },

  // ==================== UNVOICED CONSONANTS ====================
  {
    symbol: 'p',
    name: 'voiceless bilabial plosive',
    category: 'unvoiced-consonant',
    keywords: ['pen', 'top', 'happy'],
    description: 'Both lips pressed together, then released with strong burst of air. No voice.',
    spanishTip: 'Similar al español pero CON ASPIRACIÓN al inicio de palabra. "Pen" tiene un soplo de aire.',
  },
  {
    symbol: 't',
    name: 'voiceless alveolar plosive',
    category: 'unvoiced-consonant',
    keywords: ['top', 'cat', 'better'],
    description: 'Tongue tip on alveolar ridge, then releases with burst of air. No voice.',
    spanishTip: 'La lengua toca más arriba que en español. CON ASPIRACIÓN al inicio: "top" tiene soplo de aire.',
  },
  {
    symbol: 'k',
    name: 'voiceless velar plosive',
    category: 'unvoiced-consonant',
    keywords: ['cat', 'back', 'kick'],
    description: 'Back of tongue touches soft palate, then releases with burst of air. No voice.',
    spanishTip: 'Similar a "c" en "casa" pero CON ASPIRACIÓN al inicio de palabra. "Cat" tiene soplo.',
  },
  {
    symbol: 'f',
    name: 'voiceless labiodental fricative',
    category: 'unvoiced-consonant',
    keywords: ['fish', 'coffee', 'laugh'],
    description: 'Upper teeth touch lower lip lightly. Air flows through without voice.',
    spanishTip: 'Similar a la "f" española. Dientes superiores sobre el labio inferior, sin vibración de garganta.',
  },
  {
    symbol: 'θ',
    name: 'voiceless dental fricative',
    category: 'unvoiced-consonant',
    keywords: ['think', 'bath', 'tooth'],
    description: 'Tongue tip between teeth. Air flows through without voice. Continuous sound.',
    spanishTip: 'NO EXISTE en español. Lengua entre los dientes. SIN vibración. No es "t" ni "s". "Think" no es "tink" ni "sink".',
  },
  {
    symbol: 's',
    name: 'voiceless alveolar fricative',
    category: 'unvoiced-consonant',
    keywords: ['see', 'miss', 'cats'],
    description: 'Tongue near alveolar ridge. Air flows through narrow gap without voice. Sharp hissing.',
    spanishTip: 'Similar a la "s" española pero la lengua está más arriba. Sonido agudo y silbante.',
  },
  {
    symbol: 'ʃ',
    name: 'voiceless postalveolar fricative',
    category: 'unvoiced-consonant',
    keywords: ['ship', 'wash', 'nation'],
    description: 'Tongue behind alveolar ridge. Lips rounded forward. Air flows without voice.',
    spanishTip: 'Como "sh" para pedir silencio. Labios redondeados hacia adelante. No es la "ch" española.',
  },
  {
    symbol: 'tʃ',
    name: 'voiceless postalveolar affricate',
    category: 'unvoiced-consonant',
    keywords: ['church', 'match', 'nature'],
    description: 'Start like /t/, then release into /ʃ/. One single sound combining stop and fricative.',
    spanishTip: 'Similar a la "ch" española. Es un solo sonido que combina explosión y fricción.',
  },

  // ==================== OTHER CONSONANTS ====================
  {
    symbol: 'h',
    name: 'voiceless glottal fricative',
    category: 'other-consonant',
    keywords: ['hat', 'behind', 'who'],
    description: 'Air flows freely through open vocal tract. Just breath, no tongue/lip position.',
    spanishTip: 'Como la "j" suave española pero MÁS SUAVE. Solo es aire, sin fricción fuerte. Se pronuncia siempre.',
  },
  {
    symbol: 'l',
    name: 'voiced alveolar lateral approximant',
    category: 'other-consonant',
    keywords: ['light', 'feel', 'hello'],
    description: 'Tongue tip on alveolar ridge. Air flows around sides of tongue. Two types: light-L and dark-L.',
    spanishTip: 'Al inicio es "clara" (como español). Al final es "oscura": la lengua se retrae. "Feel" tiene L oscura.',
  },
  {
    symbol: 'r',
    name: 'voiced postalveolar approximant',
    category: 'other-consonant',
    keywords: ['red', 'very', 'car'],
    description: 'Tongue tip curls back slightly (not touching). In American English, r-colored everywhere.',
    spanishTip: 'NO ES vibrante como en español. La lengua NO toca nada, se enrolla ligeramente hacia atrás.',
  },
  {
    symbol: 'w',
    name: 'voiced labial-velar approximant',
    category: 'other-consonant',
    keywords: ['water', 'swim', 'queen'],
    description: 'Lips rounded and protruded, then glide to following vowel. Back of tongue raised.',
    spanishTip: 'Redondea los labios como para dar un beso, luego desliza hacia la vocal. No es "u" ni "v".',
  },
  {
    symbol: 'j',
    name: 'voiced palatal approximant',
    category: 'other-consonant',
    keywords: ['yes', 'beyond', 'use'],
    description: 'Middle of tongue raised toward hard palate. Glides into following vowel.',
    spanishTip: 'Como la "y" española en "yo" pero MÁS SUAVE, sin fricción. Es una semi-vocal, no consonante fuerte.',
  },
];
