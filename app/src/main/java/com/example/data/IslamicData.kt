package com.example.data

object IslamicData {
    val surahs: List<Surah> by lazy {
        val list = mutableListOf<Surah>()
        val famousNames = mapOf(
            1 to Triple("Al-Fatihah", "الفاتحة", 7),
            2 to Triple("Al-Baqarah", "البقرة", 286),
            3 to Triple("Ali 'Imran", "آل عمران", 200),
            4 to Triple("An-Nisa'", "النساء", 176),
            5 to Triple("Al-Ma'idah", "المائدة", 120),
            6 to Triple("Al-An'am", "الأنعام", 165),
            7 to Triple("Al-A'raf", "الأعراف", 206),
            8 to Triple("Al-Anfal", "الأنفال", 75),
            9 to Triple("At-Tawbah", "التوبة", 129),
            10 to Triple("Yunus", "يونس", 109),
            11 to Triple("Hud", "هود", 123),
            12 to Triple("Yusuf", "يوسف", 111),
            13 to Triple("Ar-Ra'd", "الرعد", 43),
            14 to Triple("Ibrahim", "ابراهيم", 52),
            15 to Triple("Al-Hijr", "الحجر", 99),
            16 to Triple("An-Nahl", "النحل", 128),
            17 to Triple("Al-Isra'", "الإسراء", 111),
            18 to Triple("Al-Kahf", "الكهف", 110),
            19 to Triple("Maryam", "مريم", 98),
            20 to Triple("Ta-Ha", "طه", 135),
            21 to Triple("Al-Anbya'", "الأنبياء", 112),
            22 to Triple("Al-Hajj", "الحج", 78),
            23 to Triple("Al-Mu'minun", "المؤمنون", 118),
            24 to Triple("An-Nur", "النور", 64),
            25 to Triple("Al-Furqan", "الفرقان", 77),
            26 to Triple("Ash-Shu'ara'", "الشعراء", 227),
            27 to Triple("An-Naml", "النمل", 93),
            28 to Triple("Al-Qasas", "القصص", 88),
            29 to Triple("Al-'Ankabut", "العنكبوت", 69),
            30 to Triple("Ar-Rum", "الروم", 60),
            31 to Triple("Luqman", "لقمان", 34),
            32 to Triple("As-Sajdah", "السجدة", 30),
            33 to Triple("Al-Ahzab", "الأحزاب", 73),
            34 to Triple("Saba'", "سبأ", 54),
            35 to Triple("Fatir", "فاطر", 45),
            36 to Triple("Ya-Sin", "يس", 83),
            37 to Triple("As-Saffat", "الصافات", 182),
            38 to Triple("Sad", "ص", 88),
            39 to Triple("Az-Zumar", "الزمر", 75),
            40 to Triple("Ghafir", "غافر", 85),
            41 to Triple("Fussilat", "فصلت", 54),
            42 to Triple("Ash-Shura", "الشورى", 53),
            43 to Triple("Az-Zukhruf", "الزخرف", 89),
            44 to Triple("Ad-Dukhan", "الدخان", 59),
            45 to Triple("Al-Jathiyah", "الجاثية", 37),
            46 to Triple("Al-Ahqaf", "الأحقاف", 35),
            47 to Triple("Muhammad", "محمد", 38),
            48 to Triple("Al-Fath", "الفتح", 29),
            49 to Triple("Al-Hujurat", "الحجرات", 18),
            50 to Triple("Qaf", "ق", 45),
            51 to Triple("Adh-Dhariyat", "الذاريات", 60),
            52 to Triple("At-Tur", "الطور", 49),
            53 to Triple("An-Najm", "النجم", 62),
            54 to Triple("Al-Qamar", "القمر", 55),
            55 to Triple("Ar-Rahman", "الرحمن", 78),
            56 to Triple("Al-Waqi'ah", "الواقعة", 96),
            57 to Triple("Al-Hadid", "الحديد", 29),
            58 to Triple("Al-Mujadilah", "المجادلة", 22),
            59 to Triple("Al-Hashr", "الحشر", 24),
            60 to Triple("Al-Mumtahanah", "الممتحنة", 13),
            61 to Triple("As-Saff", "الصف", 14),
            62 to Triple("Al-Jumu'ah", "الجمعة", 11),
            63 to Triple("Al-Munafiqun", "المنافقون", 11),
            64 to Triple("At-Taghabun", "التغابن", 18),
            65 to Triple("At-Talaq", "الطلاق", 12),
            66 to Triple("At-Tahrim", "التحريم", 12),
            67 to Triple("Al-Mulk", "الملك", 30),
            68 to Triple("Al-Qalam", "القلم", 52),
            69 to Triple("Al-Haqqah", "الحاقة", 52),
            70 to Triple("Al-Ma'arij", "المعارج", 44),
            71 to Triple("Nuh", "نوح", 28),
            72 to Triple("Al-Jinn", "الجن", 28),
            73 to Triple("Al-Muzzammil", "المزمل", 20),
            74 to Triple("Al-Muddaththir", "المدثر", 56),
            75 to Triple("Al-Qiyamah", "القيامة", 40),
            76 to Triple("Al-Insan", "الإنسان", 31),
            77 to Triple("Al-Mursalat", "المرسلات", 50),
            78 to Triple("An-Naba'", "النبأ", 40),
            79 to Triple("An-Nazi'at", "النازعات", 46),
            80 to Triple("'Abasa", "عبس", 42),
            81 to Triple("At-Takwir", "التكوير", 29),
            82 to Triple("Al-Infitar", "الانفطار", 19),
            83 to Triple("Al-Mutaffifin", "المطففين", 36),
            84 to Triple("Al-Inshiqaq", "الانشقاق", 25),
            85 to Triple("Al-Buruj", "البروج", 22),
            86 to Triple("At-Tariq", "الطارق", 17),
            87 to Triple("Al-A'la", "الأعلى", 19),
            88 to Triple("Al-Ghashiyah", "الغاشية", 26),
            89 to Triple("Al-Fajr", "الفجر", 30),
            90 to Triple("Al-Balad", "البلد", 20),
            91 to Triple("Ash-Shams", "الشمس", 15),
            92 to Triple("Al-Layl", "الليل", 21),
            93 to Triple("Ad-Duha", "الضحى", 11),
            94 to Triple("Ash-Sharh", "الشرح", 8),
            95 to Triple("At-Tin", "التين", 8),
            96 to Triple("Al-'Alaq", "العلق", 19),
            97 to Triple("Al-Qadr", "القدر", 5),
            98 to Triple("Al-Bayyinah", "البينة", 8),
            99 to Triple("Az-Zalzalah", "الزلزلة", 8),
            100 to Triple("Al-'Adiyat", "العاديات", 11),
            101 to Triple("Al-Qari'ah", "القارعة", 11),
            102 to Triple("At-Takathur", "التكاثر", 8),
            103 to Triple("Al-'Asr", "العصر", 3),
            104 to Triple("Al-Humazah", "الهمزة", 9),
            105 to Triple("Al-Fil", "الفيل", 5),
            106 to Triple("Quraysh", "قريش", 4),
            107 to Triple("Al-Ma'un", "الماعون", 7),
            108 to Triple("Al-Kauthar", "الکوثر", 3),
            109 to Triple("Al-Kafirun", "الکافرون", 6),
            110 to Triple("An-Nasr", "النصر", 3),
            111 to Triple("Al-Masad", "المسد", 5),
            112 to Triple("Al-Ikhlas", "الإخلاص", 4),
            113 to Triple("Al-Falaq", "الفلق", 5),
            114 to Triple("An-Nas", "الناس", 6)
        )

        for (id in 1..114) {
            val details = famousNames[id] ?: Triple("Surah $id", "سورة", 10)
            val revType = if (id in listOf(2, 3, 4, 5, 8, 9, 24, 33, 47, 48, 49, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 110)) "Medina" else "Mecca"
            list.add(Surah(id, details.first, details.second, details.third, revType))
        }
        list
    }

    val hadiths: List<Hadith> = listOf(
        Hadith(
            id = 1,
            chapter = "Belief",
            narrator = "Umar bin Al-Khattab",
            source = "Sahih Muslim",
            text = "Actions are judged by intentions, and every person will get what they intended.",
            arabic = "إنما الأعمال بالنيات وإنما لكل امرئ ما نوى",
            translationUrdu = "اعمال کا دارومدار نیتوں پر ہے اور ہر انسان کے لیے وہی ہے جس کی اس نے نیت کی۔",
            transliteration = "Innamal a'malu bin niyyat"
        ),
        Hadith(
            id = 2,
            chapter = "Worship",
            narrator = "Abu Hurairah",
            source = "Sahih Muslim",
            text = "The five daily prayers, and from one Friday prayer to the next, is an expiation of the sins committed in between.",
            arabic = "الصلوات الخمس والجمعة إلى الجمعة كفارة لما بينهن",
            translationUrdu = "پانچوں وقت کی نمازیں اور ایک جمعہ سے دوسرا جمعہ ان کے درمیان کے گناہوں کا کفارہ ہیں۔",
            transliteration = "As-Salawatul khamsu wal jumu'atu ilal jumu'ah"
        )
    )

    val duas: List<Dua> = emptyList()

    val namesOfAllah: List<NameOfAllah> = listOf(
        NameOfAllah(1, "الرَّحْمَنُ", "Ar-Rahman", "The Beneficent", "نہایت مہربان"),
        NameOfAllah(2, "الرَّحِيمُ", "Ar-Rahim", "The Merciful", "بڑا رحم کرنے والا"),
        NameOfAllah(3, "الْمَلِكُ", "Al-Malik", "The Eternal Lord", "حقیقی بادشاہ"),
        NameOfAllah(4, "الْقُدُّوسُ", "Al-Quddus", "The Pure", "ہر عیب سے پاک"),
        NameOfAllah(5, "السَّلَامُ", "As-Salam", "The Source of Peace", "سلامتی دینے والا"),
        NameOfAllah(6, "الْمُؤْمِنُ", "Al-Mu'min", "The Guardian of Faith", "امن و ایمان دینے والا"),
        NameOfAllah(7, "الْمُهَيْمِنُ", "Al-Muhaymin", "The Protector", "نگہبان"),
        NameOfAllah(8, "الْعَزِيزُ", "Al-Aziz", "The Almighty", "غالب و مقتدر"),
        NameOfAllah(9, "الْجَبَّارُ", "Al-Jabbar", "The Compeller", "زبردست"),
        NameOfAllah(10, "الْمُتَکَبِّرُ", "Al-Mutakabbir", "The Majestic", "کبریا اور بڑائی والا"),
        NameOfAllah(11, "الْخَالِقُ", "Al-Khaliq", "The Creator", "پیدا کرنے والا"),
        NameOfAllah(12, "الْبَارِئُ", "Al-Bari'", "The Evolver", "ٹھیک بنانے والا"),
        NameOfAllah(13, "الْمُصَوِّرُ", "Al-Musawwir", "The Fashioner", "صورت گری کرنے والا"),
        NameOfAllah(14, "الْغَفَّارُ", "Al-Ghaffar", "The Constant Forgiver", "بہت بخشنے والا"),
        NameOfAllah(15, "الْقَهَّارُ", "Al-Qahhar", "The All-Subduing", "سب پر غالب"),
        NameOfAllah(16, "الْوَهَّابُ", "Al-Wahhab", "The Supreme Bestower", "سب کچھ دینے والا"),
        NameOfAllah(17, "الرَّزَّاقُ", "Ar-Razzaq", "The Provider", "رزق دینے والا"),
        NameOfAllah(18, "الْفَتَّاحُ", "Al-Fattah", "The Supreme Solver", "کھولنے والا"),
        NameOfAllah(19, "الْعَلِيمُ", "Al-Alim", "The All-Knowing", "سب کچھ جاننے والا"),
        NameOfAllah(20, "الْقَابِضُ", "Al-Qabid", "The Withholder", "تنگی کرنے والا"),
        NameOfAllah(21, "الْبَاسِطُ", "Al-Basit", "The Extender", "فراخی کرنے والا"),
        NameOfAllah(22, "الْخَافِضُ", "Al-Khafid", "The Reducer", "پست کرنے والا"),
        NameOfAllah(23, "الرَّافِعُ", "Ar-Rafi'", "The Exalter", "بلند کرنے والا"),
        NameOfAllah(24, "الْمُعِزُّ", "Al-Mu'izz", "The Bestower of Honor", "عزت دینے والا"),
        NameOfAllah(25, "الْمُذِلُّ", "Al-Mudhill", "The Dishonorer", "ذلت دینے والا"),
        NameOfAllah(26, "السَّمِيعُ", "As-Sami'", "The All-Hearing", "سب کچھ سننے والا"),
        NameOfAllah(27, "الْبَصِيرُ", "Al-Basir", "The All-Seeing", "سب کچھ دیکھنے والا"),
        NameOfAllah(28, "الْحَکَمُ", "Al-Hakam", "The Impartial Judge", "فیصلہ کرنے والا"),
        NameOfAllah(29, "الْعَدْلُ", "Al-Adl", "The Utterly Just", "سراپا عدل و انصاف"),
        NameOfAllah(30, "اللَّطِيفُ", "Al-Latif", "The Subtle One", "نرم اور باریک بین"),
        NameOfAllah(31, "الْخَبِيرُ", "Al-Khabir", "The All-Aware", "ہر چیز سے باخبر"),
        NameOfAllah(32, "الْحَلِيمُ", "Al-Halim", "The Forbearing", "نہایت بردبار"),
        NameOfAllah(33, "الْعَظِيمُ", "Al-Azim", "The Magnificent", "بہت بڑا عظمت والا"),
        NameOfAllah(34, "الْغَفُورُ", "Al-Ghafur", "The All-Forgiving", "بہت بخشنے والا"),
        NameOfAllah(35, "الشَّکُورُ", "Ash-Shakur", "The Most Appreciative", "بڑا قدردان"),
        NameOfAllah(36, "الْعَلِيُّ", "Al-Ali", "The Sublimely Exalted", "سب سے بلند و بالا"),
        NameOfAllah(37, "الْکَبِيرُ", "Al-Kabir", "The Greatest", "بہت بڑا"),
        NameOfAllah(38, "الْحَفِيظُ", "Al-Hafiz", "The Preserver", "حفاظت کرنے والا"),
        NameOfAllah(39, "الْمُقِيتُ", "Al-Muqit", "The Sustainer", "روزی دینے والا"),
        NameOfAllah(40, "الْحَسِيبُ", "Al-Hasib", "The Reckoner", "حساب لینے والا"),
        NameOfAllah(41, "الْجَلِيلُ", "Al-Jalil", "The Majestic", "صاحب جلال"),
        NameOfAllah(42, "الْکَرِيمُ", "Al-Karim", "The Most Generous", "نہایت کرم فرمانے والا"),
        NameOfAllah(43, "الرَّقِيبُ", "Ar-Raqib", "The Watchful", "نگہبان"),
        NameOfAllah(44, "الْمُجِيبُ", "Al-Mujib", "The Responsive", "دعا قبول کرنے والا"),
        NameOfAllah(45, "الْوَاسِعُ", "Al-Wasi'", "The All-Encompassing", "وسعت والا"),
        NameOfAllah(46, "الْحَکِيمُ", "Al-Hakim", "The All-Wise", "نہایت دانا اور حکمت والا"),
        NameOfAllah(47, "الْوَدُودُ", "Al-Wadud", "The Most Loving", "بہت محبت کرنے والا"),
        NameOfAllah(48, "الْمَجِيدُ", "Al-Majid", "The Most Glorious", "بزرگی والا"),
        NameOfAllah(49, "الْبَاعِثُ", "Al-Ba'ith", "The Resurrector", "مردوں کو زندہ کرنے والا"),
        NameOfAllah(50, "الشَّهِيدُ", "Ash-Shahid", "The All-Observing", "گواہ"),
        NameOfAllah(51, "الْحَقُّ", "Al-Haqq", "The Absolute Truth", "برحق"),
        NameOfAllah(52, "الْوَکِيلُ", "Al-Wakil", "The Trustee", "کارساز"),
        NameOfAllah(53, "الْقَوِيُّ", "Al-Qawi", "The All-Strong", "نہایت طاقتور"),
        NameOfAllah(54, "الْمَتِينُ", "Al-Matin", "The Firm", "نہایت مضبوط"),
        NameOfAllah(55, "الْوَلِيُّ", "Al-Wali", "The Protecting Associate", "حامی و مددگار"),
        NameOfAllah(56, "الْحَمِيدُ", "Al-Hamid", "The All-Praiseworthy", "سزاوارِ تعریف"),
        NameOfAllah(57, "الْمُحْصِيُ", "Al-Muhsi", "The All-Enumerating", "ہر چیز کو شمار کرنے والا"),
        NameOfAllah(58, "الْمُبْدِئُ", "Al-Mubdi'", "The Originator", "پہلی بار پیدا کرنے والا"),
        NameOfAllah(59, "الْمُعِيدُ", "Al-Mu'id", "The Restorer", "دوبارہ پیدا کرنے والا"),
        NameOfAllah(60, "الْمُحْيِي", "Al-Muhyi", "The Giver of Life", "زندگی دینے والا"),
        NameOfAllah(61, "الْمُمِيتُ", "Al-Mumit", "The Bringer of Death", "موت دینے والا"),
        NameOfAllah(62, "الْحَيُّ", "Al-Hayy", "The Ever-Living", "ہمیشہ زندہ رہنے والا"),
        NameOfAllah(63, "الْقَيُّومُ", "Al-Qayyum", "The Self-Sustaining", "سب کا نگہبان اور قائم رکھنے والا"),
        NameOfAllah(64, "الْوَاجِدُ", "Al-Wajid", "The Perceiver", "پانے والا"),
        NameOfAllah(65, "الْمَاجِدُ", "Al-Majid", "The Illustrious", "بزرگی والا"),
        NameOfAllah(66, "الْوَاحِدُ", "Al-Wahid", "The One", "اکیلا"),
        NameOfAllah(67, "الأَحَدُ", "Al-Ahad", "The Unique", "ایک، تنہا"),
        NameOfAllah(68, "الصَّمَدُ", "As-Samad", "The Eternal Refuge", "بے نیاز"),
        NameOfAllah(69, "الْقَادِرُ", "Al-Qadir", "The Capable", "قادر، قدرت والا"),
        NameOfAllah(70, "الْمُقْتَدِرُ", "Al-Muqtadir", "The Omnipotent", "بڑی قدرت والا"),
        NameOfAllah(71, "الْمُقَدِّمُ", "Al-Muqaddim", "The Expediter", "آگے کرنے والا"),
        NameOfAllah(72, "الْمُؤَخِّرُ", "Al-Mu'akhkhir", "The Delayer", "پیچھے کرنے والا"),
        NameOfAllah(73, "الأَوَّلُ", "Al-Awwal", "The First", "سب سے پہلا"),
        NameOfAllah(74, "الآخِرُ", "Al-Akhir", "The Last", "سب سے آخری"),
        NameOfAllah(75, "الظَّاهِرُ", "Az-Zahir", "The Manifest", "ظاہر، عیاں"),
        NameOfAllah(76, "الْبَاطِنُ", "Al-Batin", "The Hidden", "پوشیدہ، چھپا ہوا"),
        NameOfAllah(77, "الْوَالِي", "Al-Wali", "The Governor", "مالک و سرپرست"),
        NameOfAllah(78, "الْمُتَعَالِي", "Al-Muta'ali", "The Self-Exalted", "سب سے بلند و برتر"),
        NameOfAllah(79, "الْبَرُّ", "Al-Barr", "The Source of All Goodness", "بڑا محسن اور نیک سلوک کرنے والا"),
        NameOfAllah(80, "التَّوَّابُ", "At-Tawwab", "The Ever-Relenting", "توبہ قبول کرنے والا"),
        NameOfAllah(81, "الْمُنْتَقِمُ", "Al-Muntaqim", "The Avenger", "بدلہ لینے والا"),
        NameOfAllah(82, "الْعَفُوُّ", "Al-Afuw", "The Pardoner", "بہت معاف کرنے والا"),
        NameOfAllah(83, "الرَّؤُوفُ", "Ar-Ra'uf", "The Most Kind", "نہایت شفیق"),
        NameOfAllah(84, "مَالِكُ الْمُلْكِ", "Malik-ul-Mulk", "The Owner of All Sovereignty", "تمام جہاں کا مالک"),
        NameOfAllah(85, "ذُو الْجَلَالِ وَالإِكْرَامِ", "Dhul-Jalali wal-Ikram", "The Lord of Majesty and Generosity", "جلال اور انعام والا"),
        NameOfAllah(86, "الْمُقْسِطُ", "Al-Muqsit", "The Equitable", "انصاف کرنے والا"),
        NameOfAllah(87, "الْجَامِعُ", "Al-Jami'", "The Gatherer", "جمع کرنے والا"),
        NameOfAllah(88, "الْغَنِيُّ", "Al-Ghani", "The Self-Sufficient", "بے نیاز، غنی"),
        NameOfAllah(89, "الْمُغْنِيُ", "Al-Mughni", "The Enricher", "بے نیاز کرنے والا"),
        NameOfAllah(90, "الْمَانِعُ", "Al-Mani'", "The Preventer", "روکنے والا"),
        NameOfAllah(91, "الضَّارُّ", "Ad-Darr", "The Distressor", "نقصان پہنچانے والا"),
        NameOfAllah(92, "النَّافِعُ", "An-Nafi'", "The Creator of Good", "نفع پہنچانے والا"),
        NameOfAllah(93, "النُّورُ", "An-Nur", "The Light", "روشن کرنے والا، نور"),
        NameOfAllah(94, "الْهَادِي", "Al-Hadi", "The Guide", "ہدایت دینے والا"),
        NameOfAllah(95, "الْبَدِيعُ", "Al-Badi'", "The Incomparable Originator", "انوکھا پیدا کرنے والا"),
        NameOfAllah(96, "الْبَاقِي", "Al-Baqi", "The Everlasting", "ہمیشہ رہنے والا"),
        NameOfAllah(97, "الْوَارِثُ", "Al-Warith", "The Inheritor", "سب کا وارث"),
        NameOfAllah(98, "الرَّشِيدُ", "Ar-Rashid", "The Guide to the Right Path", "ہدایت و رہنمائی کرنے والا"),
        NameOfAllah(99, "الصَّبُورُ", "As-Sabur", "The Patient", "بڑا صبر کرنے والا")
    )

    val namesOfProphet: List<NameOfAllah> = listOf(
        NameOfAllah(1, "مُحَمَّدٌ", "Muhammad", "The Praised One", "تعریف کیا گیا"),
        NameOfAllah(2, "أَحَمَدٌ", "Ahmad", "The Most Commendable", "بہت زیادہ تعریف کرنے والا"),
        NameOfAllah(3, "حَامِدٌ", "Hamid", "The Praiser", "حمد کرنے والا"),
        NameOfAllah(4, "مَحْمُودٌ", "Mahmud", "The Commended One", "پسندیدہ، ممدوح"),
        NameOfAllah(5, "قَاسِمٌ", "Qasim", "The Distributor", "تقسیم کرنے والا"),
        NameOfAllah(6, "عَاقِبٌ", "Aqib", "The Latest / Last", "سب سے آخری آنے والا"),
        NameOfAllah(7, "فَاتِحٌ", "Fatih", "The Opener", "فتح کرنے والا"),
        NameOfAllah(8, "شَاهِدٌ", "Shahid", "The Witness", "گواہی دینے والا"),
        NameOfAllah(9, "حَاشِرٌ", "Hashir", "The Gatherer", "حشر برپا کرنے والا"),
        NameOfAllah(10, "رَشِيدٌ", "Rashid", "The Guided One", "نیک، ہدایت یافتہ"),
        NameOfAllah(11, "مَشْهُودٌ", "Mashhud", "The Witnessed", "جس کی گواہی دی گئی ہو"),
        NameOfAllah(12, "بَشِيرٌ", "Bashir", "The Giver of Glad Tidings", "خوشخبری دینے والا"),
        NameOfAllah(13, "نَذِيرٌ", "Nadhir", "The Warner", "ڈرانے والا"),
        NameOfAllah(14, "دَاعٍ", "Dai", "The Inviter", "دعوت دینے والا"),
        NameOfAllah(15, "شَافٍ", "Shafi", "The Healer", "شفا دینے والا"),
        NameOfAllah(16, "هَادٍ", "Hadi", "The Guide", "ہدایت دینے والا"),
        NameOfAllah(17, "مَهْدِيٌّ", "Mahdi", "The Well-Guided One", "ہدایت یافتہ"),
        NameOfAllah(18, "مَاحٍ", "Mahi", "The Obliterator of Disbelief", "کفر مٹانے والا"),
        NameOfAllah(19, "مُنِجٍ", "Munjin", "The Deliverer", "نجات دلانے والا"),
        NameOfAllah(20, "نَاجٍ", "Naji", "The Safe One", "نجات پانے والا"),
        NameOfAllah(21, "رَسُولٌ", "Rasul", "The Messenger", "رسول، پیغام پہنچانے والا"),
        NameOfAllah(22, "نَبِيٌّ", "Nabi", "The Prophet", "غیب کی خبریں دینے والا"),
        NameOfAllah(23, "أُمِّيٌّ", "Ummi", "The Unlettered", "امی (بغیر کسی انسانی استاد کے پڑھنے والے)"),
        NameOfAllah(24, "طٰهٰ", "Taha", "O Pure and Guided One", "طہٰ"),
        NameOfAllah(25, "يٰسٓ", "Yasin", "O Perfect Man", "یاسین"),
        NameOfAllah(26, "مُزَّمِّلٌ", "Muzzammil", "The Wrapped in Garments", "چادر اوڑھنے والے"),
        NameOfAllah(27, "مُدَّثِّرٌ", "Muddaththir", "The Shrouded in Cloak", "لحاف میں لپٹنے والے"),
        NameOfAllah(28, "شَفِيعٌ", "Shafi'", "The Intercessor", "شفاعت کرنے والا"),
        NameOfAllah(29, "مُصَحِّحٌ", "Musahhih", "The Rectifier", "تصحیح کرنے والا"),
        NameOfAllah(30, "مُطَهَّرٌ", "Mutahhar", "The Purified One", "پاک کیا گیا"),
        NameOfAllah(31, "طَيِّبٌ", "Tayyib", "The Pure", "پاکیزہ، عمدہ"),
        NameOfAllah(32, "سَيِّدٌ", "Sayyid", "The Leader", "سردار"),
        NameOfAllah(33, "حَبِيبٌ", "Habib", "The Beloved", "محبوب، پیارا"),
        NameOfAllah(34, "خَلِيلٌ", "Khalil", "The Intimate Friend", "گہرا دوست، خلیل"),
        NameOfAllah(35, "صَفِيٌّ", "Safi", "The Chosen One", "مصفا، منتخب"),
        NameOfAllah(36, "مُخْتَارٌ", "Mukhtar", "The Selected One", "بااختیار، چنا ہوا"),
        NameOfAllah(37, "مُصْطَفَى", "Mustafa", "The Chosen Messenger", "چنا ہوا، منتخب"),
        NameOfAllah(38, "مُجْتَبَى", "Mujtaba", "The Selected One", "چنیدہ"),
        NameOfAllah(39, "مُرْتَضَى", "Murtada", "The Accepted One", "پسندیدہ، راضی کیا گیا"),
        NameOfAllah(40, "صَادِقٌ", "Sadiq", "The Truthful", "سچا"),
        NameOfAllah(41, "أَمِينٌ", "Amin", "The Trustworthy", "امانت دار"),
        NameOfAllah(42, "مُصَدِّقٌ", "Musaddiq", "The Confirmer", "تصدیق کرنے والا"),
        NameOfAllah(43, "مُطَهِّرٌ", "Mutahhir", "The Purifier", "پاک کرنے والا"),
        NameOfAllah(44, "ظَاهِرٌ", "Zahir", "The Manifest", "غالب، ظاہر"),
        NameOfAllah(45, "بَاطِنٌ", "Batin", "The Internal / Hidden", "باطنی، پوشیدہ"),
        NameOfAllah(46, "قَرِيبٌ", "Qarib", "The Near One", "قریب"),
        NameOfAllah(47, "وَاصِلٌ", "Wasil", "The Connected One", "ملاپ کرانے والا، واصل"),
        NameOfAllah(48, "مَوْصُولٌ", "Mawsul", "The Conjoined", "واصل باللہ"),
        NameOfAllah(49, "سَابِقٌ", "Sabiq", "The Foremost", "سبقت لے جانے والا"),
        NameOfAllah(50, "هَادٍ", "Hadi", "The Leader / Guide", "رہبر، ہادی"),
        NameOfAllah(51, "مُهْدٍ", "Muhdin", "The Presenter of Divine Gift", "ہدیہ دینے والا"),
        NameOfAllah(52, "مُقَدَّسٌ", "Muqaddas", "The Sacred One", "پاکیزہ، مقدس"),
        NameOfAllah(53, "رَؤُوفٌ", "Rauf", "The Compassionate", "نہایت مہربان"),
        NameOfAllah(54, "رَحِيمٌ", "Rahim", "The Merciful", "رحم دل"),
        NameOfAllah(55, "جَوَّادٌ", "Jawwad", "The Generous One", "سخی"),
        NameOfAllah(56, "كَرِيمٌ", "Karim", "The Noble / Generous", "صاحب کرم، سخی"),
        NameOfAllah(57, "قَوِيٌّ", "Qawi", "The Strong", "قوی، طاقتور"),
        NameOfAllah(58, "حَفِيظٌ", "Hafiz", "The Preserver", "محافظ، حفاظت کرنے والا"),
        NameOfAllah(59, "شَهِيرٌ", "Shahir", "The Famous", "مشهور"),
        NameOfAllah(60, "مُنِيرٌ", "Munir", "The Illuminant", "روشن کرنے والا"),
        NameOfAllah(61, "مُبَلِّغٌ", "Muballigh", "The Preacher / Conveyer", "پیغام پہنچانے والا"),
        NameOfAllah(62, "نَاصِرٌ", "Nasir", "The Helper", "مددگار"),
        NameOfAllah(63, "مَنْصُورٌ", "Mansur", "The Victorious One", "فتح یاب، مدد کیا گیا"),
        NameOfAllah(64, "طَيِّبٌ", "Tayyib", "The Clean / Pure", "پاک، طیب"),
        NameOfAllah(65, "مِدادٌ", "Midad", "The Resourceful", "مدد فراہم کرنے والا"),
        NameOfAllah(66, "عَادِلٌ", "Adil", "The Just", "عادل، انصاف پسند"),
        NameOfAllah(67, "شَكُورٌ", "Shakur", "The Most Grateful", "قدردان، شکر گزار"),
        NameOfAllah(68, "وَلِيٌّ", "Wali", "The Friend / Guardian", "دوست، حامی"),
        NameOfAllah(69, "بَدِيعٌ", "Badi'", "The Unique Originator", "بے مثل، انوکھا"),
        NameOfAllah(70, "كَامِلٌ", "Kamil", "The Perfect One", "کامل، مکمل"),
        NameOfAllah(71, "شَافٍ", "Shafi", "The Satisfying Healer", "تسلی دینے والا، شفا بخش"),
        NameOfAllah(72, "شَاكِرٌ", "Shakir", "The Thankful One", "شکر گزار"),
        NameOfAllah(73, "قَارِئٌ", "Qari", "The Reciter of Quran", "قرأت کرنے والا"),
        NameOfAllah(74, "مُتَوَكِّلٌ", "Mutawakkil", "The Reliant on Allah", "توکل کرنے والا"),
        NameOfAllah(75, "مُكْتَفٍ", "Muktafi", "The Content One", "کفایت کرنے والا"),
        NameOfAllah(76, "شَفِيعٌ", "Shafi'", "The Advocate", "شفاعت کرنے والا"),
        NameOfAllah(77, "مُصْلِحٌ", "Muslih", "The Reformer", "اصلاح کرنے والا"),
        NameOfAllah(78, "مُطِيعٌ", "Mutee", "The Obedient One", "فرمانبردار"),
        NameOfAllah(79, "وَاسِطٌ", "Wasit", "The Mediator", "وسیله بننے والا"),
        NameOfAllah(80, "رَحْمَةٌ", "Rahmah", "The Divine Mercy", "رحمت"),
        NameOfAllah(81, "أُمَّةٌ", "Ummat", "The Leader of the Nation", "امت کے رہبر"),
        NameOfAllah(82, "مُجِيبٌ", "Mujib", "The Answerer", "جواب دینے والا"),
        NameOfAllah(83, "مُعِزٌّ", "Mu'izz", "The Giver of Honor", "عزت دینے والا"),
        NameOfAllah(84, "سَعِيدٌ", "Sa'id", "The Fortunate One", "نیک بخت، خوش نصیب"),
        NameOfAllah(85, "صَابِرٌ", "Sabir", "The Patient One", "صبر کرنے والا"),
        NameOfAllah(86, "شَاكِرٌ", "Shakir", "The Gratifying / Appreciative", "قدر کرنے والا"),
        NameOfAllah(87, "هَاشِمِيٌّ", "Hashimi", "The Hashimite", "ہاشمی خاندان سے"),
        NameOfAllah(88, "أَبْطَحِيٌّ", "Abtahi", "Of Abtah (Mecca)", "ابطحی (مکہ کے میدان سے منسوب)"),
        NameOfAllah(89, "حِجَازِيٌّ", "Hijazi", "Of Hejaz", "حجازی"),
        NameOfAllah(90, "نَزَرِيٌّ", "Nazari", "The Sightful Observer", "صاحب بصیرت"),
        NameOfAllah(91, "مَدَنِيٌّ", "Madani", "Of Madinah", "مدنی"),
        NameOfAllah(92, "أُمِّيٌّ", "Ummi", "The Unlettered Prophet", "نبیِ امی"),
        NameOfAllah(93, "عَزِيزٌ", "Aziz", "The Beloved / Dear", "پیارا، عزیز"),
        NameOfAllah(94, "حَرِيصٌ", "Haris", "The Eager for Our Salvation", "بھلائی کے خواہشمند"),
        NameOfAllah(95, "بِالْمُؤْمِنِينَ", "Bil-Muminina", "Affectionate to Believers", "ایمان والوں کے لیے"),
        NameOfAllah(96, "رَؤُوفٌ رَحِيمٌ", "Rauf-ur-Rahim", "Kind and Merciful", "شفیق اور مہربان"),
        NameOfAllah(97, "طه", "Taha", "Taha", "طہٰ"),
        NameOfAllah(98, "يس", "Yasin", "Yasin", "یاسین"),
        NameOfAllah(99, "سِرَاجٌ مُنِيرٌ", "Siraj-um-Munir", "The Glowing Lamp", "روشن چراغ")
    )

    fun matchesSurah(surah: Surah, query: String): Boolean {
        val qCleaned = query.lowercase().trim()
        if (qCleaned.isEmpty()) return false
        
        // Direct checks
        if (surah.id.toString() == qCleaned) return true
        if (surah.name.lowercase().contains(qCleaned) || surah.nameArabic.contains(query)) return true

        // Clean query and surah name phonetically
        fun phoneticNormalize(str: String): String {
            return str.lowercase()
                .replace("fateha", "fatiha")
                .replace("fatah", "fatiha")
                .replace("baqra", "baqara")
                .replace("kahaf", "kahf")
                .replace("rehman", "rahman")
                .replace("yaseen", "yasin")
                .replace("yasen", "yasin")
                .replace("tauha", "taha")
                .replace("toha", "taha")
                .replace("nisaa", "nisa")
                .replace("naas", "nas")
                .replace("waqia", "waqiah")
                .replace("sajda", "sajdah")
        }

        fun normalize(str: String): String {
            val phon = phoneticNormalize(str)
            return phon
                .replace("-", " ") // replace dash with space
                .replace("'", "")  // remove apostrophe
                .replace("`", "")  // remove backtick
                .replace("’", "")  // remove curly quote
                .replace(Regex("[^a-z0-9 ]"), "") // keep alphanumeric and space
                .replace("ee", "i") // yaseen -> yasin (fail-safe)
                .replace("oo", "u") // soorah -> surah
                .replace("aa", "a") // naas -> nas
                .replace("ah ", " ") 
                .replace(Regex("ah$"), "a") // ending ah -> a (fatihah -> fatiha, baqarah -> baqara)
                .replace("kh", "h") // allow kaf/kahf fuzzy match
                .replace("ph", "f")
                .trim()
        }

        val qNorm = normalize(qCleaned)
        val sNorm = normalize(surah.name)
        if (sNorm.contains(qNorm) || qNorm.contains(sNorm)) return true

        // Tokenize and filter out "surah" / "surat" and articles
        val stopwords = setOf("surah", "surat", "sura", "soorah", "soorat", "sorah", "chapter", "surat-", "surah-")
        val articles = setOf("al", "an", "ar", "as", "at", "ad", "az", "ash", "el", "the", "ali")

        val qTokens = qNorm.split(Regex("\\s+")).filter { it.isNotEmpty() && !stopwords.contains(it) }
        val sTokens = sNorm.split(Regex("\\s+")).filter { it.isNotEmpty() && !stopwords.contains(it) && !articles.contains(it) }

        if (qTokens.isEmpty()) return false

        // Filter out articles from query tokens too, unless it leaves the query empty
        val qTokensNoArticles = qTokens.filter { !articles.contains(it) }
        val activeQTokens = if (qTokensNoArticles.isNotEmpty()) qTokensNoArticles else qTokens

        val sJoined = sTokens.joinToString("")
        val qJoined = activeQTokens.joinToString("")
        
        if (sJoined.contains(qJoined) || qJoined.contains(sJoined)) return true

        // Check if all active query tokens are prefixes or sub-parts of the surah tokens
        return activeQTokens.all { qT ->
            sTokens.any { sT -> sT.contains(qT) || qT.contains(sT) }
        }
    }
}
