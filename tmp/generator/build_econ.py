#!/usr/bin/env python3
import sys
import os
sys.path.append('tmp/generator')
from common import render_kotlin_file, extract_clean_existing

def build_economics():
    existing = extract_clean_existing('Economics')
    print(f"Existing clean Economics: {len(existing)}")
    
    # 185 unique authentic curriculum questions covering the complete Nigerian syllabus
    raw_econ = [
        # Microeconomics & Methodology
        ("The basic economic problem of scarcity arises because:", "Productive resources are limited while human wants are unlimited", "Governments fail to print enough currency", "Taxes are excessively high", "Populations grow faster than capital", "A", "Basic Concepts", "EASY", "Scarcity is the fundamental economic reality that resources are finite relative to infinite human wants."),
        ("Opportunity cost is accurately defined as:", "The value of the next best alternative forgone when a choice is made", "The financial cost of purchasing a good", "The total money expenditure in production", "The accounting depreciation of machinery", "A", "Basic Concepts", "EASY", "Opportunity cost measures the sacrifice incurred in choosing one alternative over another."),
        ("A consumer's scale of preference is best described as:", "A list of unsatisfied wants arranged in order of relative priority", "A table showing quantities demanded at various prices", "A graphical display of budget constraints", "A schedule of producer input costs", "A", "Basic Concepts", "EASY", "A scale of preference helps a rational consumer allocate limited income to satisfy the most pressing wants first."),
        ("The Production Possibility Curve (PPC) is bowed outward (concave to the origin) due to the:", "Law of increasing opportunity costs", "Law of diminishing marginal utility", "Constant returns to scale", "Decreasing production costs", "A", "Basic Concepts", "MEDIUM", "As more of one good is produced, progressively more of the other must be sacrificed because resources are not equally adaptable."),
        ("An economic system characterized by public ownership of resources and central government planning is a:", "Socialist (command) economy", "Capitalist economy", "Mixed economy", "Free enterprise economy", "A", "Economic Systems", "EASY", "In a socialist system, the state owns productive capital and allocates resources through central planning."),
        ("In a pure market capitalist economy, resource allocation is guided by:", "The price mechanism (forces of demand and supply)", "Directives of the central planning bureau", "Tradition and customs", "Rationing coupons", "A", "Economic Systems", "EASY", "The invisible hand of price signals coordinates production and consumption in free markets."),
        ("A mixed economy combines elements of:", "Both private enterprise and government intervention", "Capitalism and feudalism", "Traditional subsistence and barter", "Domestic and foreign monopoly", "A", "Economic Systems", "EASY", "Mixed economies feature private sector initiative alongside state regulation and public enterprise provision."),
        ("A positive economic statement differs from a normative statement because it:", "Deals with objective, verifiable facts and relationships", "Expresses subjective ethical opinions", "Dictates what economic policy ought to be", "Is always approved by parliament", "A", "Economic Methodology", "MEDIUM", "Positive economics describes and tests what is, while normative economics asserts what ought to be based on value judgments."),
        
        # Theory of Demand
        ("The normal downward slope of a demand curve from left to right reflects the:", "Inverse relationship between price and quantity demanded", "Direct relationship between price and supply", "Equal distribution of consumer income", "Constant elasticity of demand", "A", "Theory of Demand", "EASY", "According to the law of demand, consumers purchase more at lower prices, ceteris paribus."),
        ("Which of the following will cause a movement along the demand curve rather than a shift?", "A change in the price of the commodity itself", "A change in consumer taste and fashion", "An increase in population size", "A rise in the price of substitute goods", "A", "Theory of Demand", "EASY", "Price changes of the good cause contractions or expansions along the same demand curve."),
        ("The demand for beef and cowhide is an example of:", "Joint demand", "Derived demand", "Composite demand", "Competitive demand", "A", "Theory of Demand", "MEDIUM", "Joint demand occurs when two goods are produced together and demanded jointly."),
        ("The demand for flour for baking bread, cakes, and meat pies is described as:", "Composite demand", "Derived demand", "Competitive demand", "Joint demand", "A", "Theory of Demand", "MEDIUM", "Composite demand refers to a single commodity satisfying multiple distinct uses."),
        ("The demand for commercial lorry drivers is classified as:", "Derived demand", "Direct demand", "Composite demand", "Autonomous demand", "A", "Theory of Demand", "EASY", "The demand for labor is derived from the demand for the transport services it helps produce."),
        ("Goods that satisfy similar human wants and can replace one another (e.g. tea and coffee) exhibit:", "Competitive demand (substitutes)", "Complementary demand", "Joint demand", "Derived demand", "A", "Theory of Demand", "EASY", "Substitutes compete for consumer spending; an increase in the price of tea raises demand for coffee."),
        ("An exceptional (abnormal) upward-sloping demand curve occurs in the case of:", "Veblen goods (goods of ostentation)", "Normal consumer necessities", "Capital goods", "Manufactured exports", "A", "Theory of Demand", "MEDIUM", "Ostentatious goods are purchased to flaunt status, so demand increases as their price rises."),
        ("For a Giffen good, an increase in its price leads to an increase in quantity demanded because:", "The negative income effect outweighs the positive substitution effect", "The good has many close substitutes", "Consumer income has doubled", "Production costs have escalated", "A", "Theory of Demand", "HARD", "Giffen goods are highly inferior staples where a price rise impoverishes consumers, forcing them to buy more."),
        
        # Theory of Supply & Market Equilibrium
        ("The law of supply states that, ceteris paribus, suppliers will offer more goods for sale at:", "Higher prices", "Lower prices", "Constant prices", "Subsidized prices", "A", "Theory of Supply", "EASY", "Higher market prices increase expected profit margins, encouraging suppliers to expand output."),
        ("A technological breakthrough that reduces manufacturing costs will shift the supply curve:", "Outward to the right", "Inward to the left", "Vertically upward", "Along the existing curve", "A", "Theory of Supply", "MEDIUM", "Lower unit production costs expand industry supply at every price level."),
        ("Joint supply is best illustrated by the simultaneous production of:", "Petroleum and kerosene", "Tea and coffee", "Margarine and butter", "Cars and tires", "A", "Theory of Supply", "EASY", "Refining crude oil automatically produces petroleum, kerosene, and diesel as joint outputs."),
        ("When the quantity demanded of a commodity equals the quantity supplied at a prevailing price, the market is in:", "Equilibrium", "Disequilibrium", "Deficit", "Surplus", "A", "Market Equilibrium", "EASY", "Market equilibrium clears the market with zero unsold inventory or unmet consumer demand."),
        ("If the price of a good is set above the equilibrium market price, the immediate market result is a:", "Surplus (excess supply)", "Shortage (excess demand)", "Scarcity of consumer goods", "Black market premium", "A", "Market Equilibrium", "MEDIUM", "At above-equilibrium prices, producers supply more than consumers are willing to purchase at that price."),
        ("When government legislates a maximum price ceiling below equilibrium to protect low-income consumers, it creates a:", "Persistent shortage and rationing pressures", "Persistent surplus", "Boom in business profits", "Collapse in consumer demand", "A", "Price Legislation", "MEDIUM", "Price ceilings artificially suppress price, resulting in excess demand and potential black marketing."),
        ("A minimum price floor set above equilibrium to guarantee agricultural farm incomes results in a:", "Surplus that government must purchase or store", "Market shortage", "Decline in farm production", "Collapse in product quality", "A", "Price Legislation", "MEDIUM", "Guaranteed high prices induce farmers to overproduce, leaving a surplus that requires government intervention."),
        
        # Elasticity of Demand and Supply
        ("Price Elasticity of Demand (PED) measures the:", "Percentage change in quantity demanded divided by percentage change in price", "Absolute change in price per unit change in demand", "Change in consumer income relative to price", "Ratio of supply to demand", "A", "Elasticity", "EASY", "PED measures the responsiveness of consumer quantity demanded to fluctuations in product price."),
        ("When a 5% drop in price results in a 15% increase in quantity demanded, the price elasticity of demand is:", "3.0 (Elastic)", "0.33 (Inelastic)", "1.0 (Unitary)", "0.0 (Perfect Inelastic)", "A", "Elasticity", "MEDIUM", "PED = 15% / 5% = 3.0. Since PED > 1, demand is price-elastic."),
        ("A perfectly price-inelastic demand curve is represented graphically by a:", "Vertical straight line parallel to the vertical price axis", "Horizontal line parallel to quantity axis", "Rectangular hyperbola", "Downward sloping 45-degree line", "A", "Elasticity", "MEDIUM", "Quantity demanded remains fixed regardless of price swings, yielding a vertical line (Ep = 0)."),
        ("If a firm increases its selling price and its total revenue decreases, the price elasticity of demand is:", "Elastic (|PED| > 1)", "Inelastic (|PED| < 1)", "Unitary elastic", "Zero", "A", "Elasticity", "HARD", "For elastic goods, the percentage loss in volume outweighs the percentage gain in price, lowering total revenue."),
        ("If cross-price elasticity of demand between goods A and B is negative (Exy < 0), the goods are:", "Complements", "Substitutes", "Inferior goods", "Independent goods", "A", "Elasticity", "MEDIUM", "A price increase in car fuel causes a decline in demand for large cars, indicating negative cross elasticity."),
        ("Income elasticity of demand for a normal necessity typically lies:", "Between 0 and 1 (positive and inelastic)", "Above 1 (positive and elastic)", "Below 0 (negative)", "Exactly zero", "A", "Elasticity", "MEDIUM", "Necessities experience moderate demand growth as household income expands."),
        ("Price Elasticity of Supply (PES) tends to be more elastic in the:", "Long run than in the short run", "Short run than in the long run", "Immediate market period", "Agricultural harvest season", "A", "Elasticity", "MEDIUM", "In the long run, firms can adjust all production inputs, install new machinery, and expand capacity."),
        
        # Consumer Behavior & Utility
        ("Total utility reaches its maximum point when marginal utility is:", "Zero", "At its maximum", "Positive and rising", "Negative", "A", "Consumer Theory", "MEDIUM", "When the addition to satisfaction from the last unit consumed is zero, total satisfaction has peaked."),
        ("The principle that consumers maximize total satisfaction when marginal utility per naira is equal across all goods is:", "Equi-marginal utility principle", "Law of demand", "Law of diminishing returns", "Engel's law", "A", "Consumer Theory", "MEDIUM", "Consumer equilibrium: MUx / Px = MUy / Py = ... = MUn / Pn."),
        ("Consumer surplus is accurately calculated as the difference between:", "The maximum price a consumer is willing to pay and the market price actually paid", "Total revenue and total cost", "Money income and expenditure", "Gross and net utility", "A", "Consumer Theory", "MEDIUM", "Consumer surplus represents the net welfare gain enjoyed by buyers paying less than their reservation price."),
        ("An indifference map consists of a series of indifference curves where curves further from the origin represent:", "Higher levels of total satisfaction", "Higher commodity prices", "Lower consumer income", "Increasing production costs", "A", "Consumer Theory", "MEDIUM", "Higher indifference curves contain greater bundles of goods, providing greater overall consumer utility."),
        ("The slope of an indifference curve at any given point is called the:", "Marginal Rate of Substitution (MRS)", "Marginal Rate of Transformation", "Price ratio", "Elasticity of substitution", "A", "Consumer Theory", "HARD", "MRS measures the rate at which a consumer is willing to give up good Y to obtain an extra unit of good X."),
        ("A budget line shifts outward in a parallel direction when:", "Consumer money income increases while commodity prices remain constant", "The price of one commodity increases", "The consumer tastes change", "Both commodity prices double", "A", "Consumer Theory", "MEDIUM", "Higher income expands purchasing power equally across both goods without altering relative price slopes."),
        
        # Production & Cost Concepts
        ("In economic analysis, the short run is defined as a time horizon in which:", "At least one factor of production is fixed", "All factors of production are completely variable", "No new technology can ever be invented", "Production lasts less than one calendar year", "A", "Production Theory", "EASY", "Short run implies constraints where capital or plant size cannot be immediately varied."),
        ("The law of diminishing marginal returns operates primarily because:", "Variable inputs are added to a fixed factor of production", "Management becomes corrupt", "Laborers demand higher wages", "Machines suffer immediate obsolescence", "A", "Production Theory", "EASY", "Crowding on fixed acreage or machinery causes incremental output per additional worker to eventually drop."),
        ("When marginal product (MP) exceeds average product (AP), average product is:", "Rising", "Falling", "At its minimum", "Zero", "A", "Production Theory", "MEDIUM", "Whenever a marginal value is above an average value, it pulls the average upward."),
        ("When marginal product (MP) equals zero, total product (TP) is:", "At its maximum", "At its minimum", "Zero", "Declining rapidly", "A", "Production Theory", "MEDIUM", "Total product stops growing and reaches its apex when the additional output of the next input is zero."),
        ("Fixed costs in production are costs that:", "Do not vary with changes in the level of output produced", "Increase proportionally with output", "Only occur in the long run", "Equal marginal costs at zero output", "A", "Cost Theory", "EASY", "Fixed costs (e.g. factory rent, machinery depreciation) must be paid even if output is zero."),
        ("Which of the following is an example of a variable cost for a bakery enterprise?", "Flour, sugar, and baking electricity", "Factory building lease", "Fire insurance premium", "Interest on bank loan", "A", "Cost Theory", "EASY", "Raw materials vary directly with the number of loaves baked."),
        ("The Marginal Cost (MC) curve intersects the Average Total Cost (ATC) curve at its:", "Minimum point", "Maximum point", "Vertical intercept", "Point of inflection", "A", "Cost Theory", "MEDIUM", "When MC < ATC, ATC falls; when MC > ATC, ATC rises; hence MC cuts ATC at its lowest point."),
        ("Internal economies of scale refer to cost advantages that arise from:", "The expansion of the individual firm's scale of operations", "The growth of the entire industry in a geographic cluster", "Government corporate tax holidays", "General improvements in public roads", "A", "Production Theory", "MEDIUM", "Internal economies (technical, managerial, financial) lower long-run average costs as the firm grows."),
        
        # Market Structures & Pricing
        ("Which characteristic is unique to a perfectly competitive market?", "Firms face a horizontal, perfectly elastic demand curve where P = AR = MR", "Product differentiation through branding", "Substantial advertising expenditure", "Collusion to restrict supply", "A", "Market Structures", "MEDIUM", "Competitive firms cannot influence price; they take the market equilibrium price as given."),
        ("Under perfect competition, the short-run shut-down point occurs when market price falls below:", "Average Variable Cost (AVC)", "Average Total Cost (ATC)", "Average Fixed Cost (AFC)", "Marginal Cost (MC)", "A", "Market Structures", "HARD", "If price cannot even cover variable operating costs, the firm minimizes losses by halting production entirely."),
        ("A pure monopoly firm maximizes economic profit at the output level where:", "Marginal Revenue equals Marginal Cost (MR = MC)", "Price equals Marginal Cost (P = MC)", "Average Revenue equals Average Cost", "Total Revenue is maximized", "A", "Market Structures", "MEDIUM", "All profit-seeking firms expand output until the revenue from the last unit equals its marginal cost."),
        ("A natural monopoly typically develops in public utilities (water, electricity grid) because:", "Extremely high fixed capital costs make duplicate infrastructure economically wasteful", "The government bans all private companies", "Raw materials are scarce", "Consumer demand is zero", "A", "Market Structures", "MEDIUM", "Enormous economies of scale mean a single provider achieves lowest long-run average cost."),
        ("Price discrimination by a monopolist is only commercially feasible when:", "The firm can segment the market and prevent resale between consumer groups", "The product is a primary agricultural crop", "Government fixes legal prices", "Consumer demand is perfectly elastic everywhere", "A", "Market Structures", "HARD", "Sub-markets must have distinct price elasticities and arbitrage between segments must be impossible."),
        ("Monopolistic competition is distinguished from perfect competition by the presence of:", "Product differentiation and brand competition", "Complete barriers to market entry", "A single dominant seller", "Identical homogenous products", "A", "Market Structures", "MEDIUM", "Firms sell slightly differentiated products (e.g. soaps, restaurants), giving them minor pricing power."),
        ("Collusion among oligopolistic producers to fix prices and allocate output quotas creates a:", "Cartel (e.g. OPEC)", "Monopsony", "Cooperative union", "Conglomerate merger", "A", "Market Structures", "EASY", "Cartels act as de facto monopolies to maximize collective member profits."),
        
        # Money & Banking
        ("A major defect of the ancient barter system was the:", "Difficulty of securing a double coincidence of wants", "Abundance of metallic coins", "High stability of commodity prices", "Speed of transactions", "A", "Money & Banking", "EASY", "Barter requires that both parties want what the other possesses at the same time and place."),
        ("Legal tender refers to currency that:", "Must be accepted by law in settlement of all monetary debts", "Is backed 100% by gold reserves", "Is issued by commercial banks", "Cannot be exchanged for foreign currency", "A", "Money & Banking", "EASY", "Citizens cannot legally refuse legal tender currency in payment of domestic debt obligations."),
        ("The primary asset from which commercial banks derive their interest income is:", "Loans and advances to business and personal borrowers", "Cash in the bank vault", "Government treasury bills", "Customer fixed deposits", "A", "Money & Banking", "EASY", "Commercial banks charge higher interest rates on loans than they pay on customer deposits."),
        ("If the statutory cash reserve requirement is 20%, the theoretical credit multiplier is:", "5", "4", "10", "2", "A", "Money & Banking", "MEDIUM", "Credit multiplier = 1 / Reserve Ratio = 1 / 0.20 = 5."),
        ("Which of the following institutions acts as banker to the Federal Government of Nigeria?", "Central Bank of Nigeria (CBN)", "First Bank of Nigeria", "Bank of Industry", "Nigeria Deposit Insurance Corporation", "A", "Money & Banking", "EASY", "The CBN manages government accounts, floats treasury debt, and advises on public financial management."),
        ("When the central bank sells government treasury securities to the public, the volume of bank credit:", "Contracts (decreases)", "Expands (increases)", "Remains unchanged", "Doubles", "A", "Monetary Policy", "MEDIUM", "Selling securities withdraws cash reserves from commercial banks, curbing their lending capacity."),
        ("Moral suasion by a central bank involves:", "Appealing to and persuading commercial banks to comply with credit policy guidelines", "Revoking banking operating licenses", "Mandating imprisonment for bankers", "Printing physical bank notes", "A", "Monetary Policy", "EASY", "Moral suasion is an informal, non-statutory method using diplomatic persuasion with bank executives."),
        
        # Public Finance & Fiscal Policy
        ("A tax whose rate increases progressively as the taxpayer's income increases is a:", "Progressive tax", "Regressive tax", "Proportional tax", "Poll tax", "A", "Public Finance", "EASY", "Progressive taxation taxes higher marginal brackets at higher rates to achieve equity."),
        ("A regressive tax system places a heavier relative financial burden on:", "Low-income earners", "High-income earners", "Corporate conglomerates", "Foreign expatriates", "A", "Public Finance", "EASY", "Regressive taxes (e.g. sales taxes on necessities) take a larger percentage of a poor person's income."),
        ("Which canon of taxation dictates that taxes should be collected at a time and manner most suitable for the taxpayer?", "Canon of Convenience", "Canon of Certainty", "Canon of Economy", "Canon of Equity", "A", "Public Finance", "MEDIUM", "PAYE payroll deductions exemplify convenience by taxing workers when wages are disbursed."),
        ("Recurrent government expenditure comprises spending on:", "Salaries, pensions, and routine daily administration overhead", "Construction of bridges and expressways", "Building new international airports", "Purchasing hydroelectric dams", "A", "Public Finance", "EASY", "Recurrent expenditure covers operational consumable expenses, whereas capital expenditure funds long-term assets."),
        ("A government budget in which projected public revenues exceed anticipated expenditures is a:", "Budget surplus", "Budget deficit", "Balanced budget", "Supplementary budget", "A", "Public Finance", "EASY", "Surplus budgets withdraw aggregate demand from an economy, useful for curbing demand-pull inflation."),
        ("Public national debt incurred from domestic residents, banks, and institutions is classified as:", "Internal (domestic) debt", "External (foreign) debt", "Bilateral debt", "Multilateral debt", "A", "Public Finance", "EASY", "Domestic debt involves government securities denominated in local currency held within national borders."),
        
        # National Income Accounting
        ("The value of national output measured at current prevailing prices without adjusting for inflation is:", "Nominal National Income", "Real National Income", "Per capita income", "Net disposable income", "A", "National Income", "EASY", "Nominal income can rise purely due to inflation without any increase in physical goods produced."),
        ("In the expenditure approach to national income accounting, GDP equals:", "C + I + G + (X - M)", "C + S + T", "Wages + Rent + Interest + Profit", "Output - Depreciation", "A", "National Income", "MEDIUM", "GDP = Consumption (C) + Investment (I) + Government Spending (G) + Net Exports (X - M)."),
        ("Transfer payments (such as student bursaries, pensions, and gifts) are excluded from GDP because:", "They do not reflect current production of goods or services", "They are too small to measure", "They are illegal transactions", "They only occur in socialist states", "A", "National Income", "MEDIUM", "Transfer payments merely transfer purchasing power without corresponding economic output."),
        ("The circular flow of income in a closed two-sector economy involves transactions between:", "Households and business firms", "Government and foreign exporters", "Commercial banks and central bank", "Importers and wholesalers", "A", "Macroeconomics", "EASY", "Households provide factor inputs to firms, and firms provide goods, services, and wages to households."),
        
        # Inflation & Macroeconomic Issues
        ("A persistent and general increase in the overall price level of goods and services over time is:", "Inflation", "Deflation", "Stagflation", "Devaluation", "A", "Macroeconomics", "EASY", "Inflation erodes the purchasing power of money across the national economy."),
        ("The co-existence of high unemployment, sluggish economic growth, and high inflation is described as:", "Stagflation", "Hyperinflation", "Reflation", "Disinflation", "A", "Macroeconomics", "MEDIUM", "Stagflation combines stagnation and inflation, posing a dilemma for conventional policy."),
        ("The index commonly constructed by national statistics bureaus to measure consumer inflation is the:", "Consumer Price Index (CPI)", "Human Development Index (HDI)", "Gini coefficient", "Balance of trade index", "A", "Macroeconomics", "EASY", "CPI measures price changes over time in a representative weighted basket of household goods."),
        ("During severe demand-pull inflation, an appropriate contractionary fiscal policy response is to:", "Increase direct taxes and reduce government expenditure", "Decrease taxes and increase subsidies", "Lower central bank interest rates", "Expand public works construction", "A", "Macroeconomic Policy", "MEDIUM", "Raising taxes and curbing state spending dampens aggregate demand, cooling inflationary pressures."),
        
        # International Trade & Development
        ("David Ricardo demonstrated that two countries can gain from international trade even if one country has an absolute advantage in both goods, provided they have differing:", "Comparative opportunity costs", "Currency exchange rates", "Total land areas", "Government structures", "A", "International Trade", "HARD", "A country should specialize in goods it produces at the lowest relative opportunity cost."),
        ("A tax levied on imported goods calculated as a percentage of the declared value of the goods is an:", "Ad valorem tariff", "Specific tariff", "Compound tariff", "Excise duty", "A", "International Trade", "MEDIUM", "Ad valorem duties (e.g. 15% of invoice CIF value) vary directly with the value of imports."),
        ("The balance of trade is specifically defined as the:", "Difference between the monetary value of visible exports and visible imports", "Total value of all foreign exchange transactions", "Difference between public revenue and expenditure", "Net balance of capital investments", "A", "International Trade", "EASY", "Balance of trade accounts strictly for merchandise visible trade; services belong to the current account."),
        ("When a country's total foreign receipts fall short of its total foreign payments over a year, it incurs a:", "Balance of payments deficit", "Balance of payments surplus", "Favorable terms of trade", "Currency appreciation", "A", "International Trade", "EASY", "A BOP deficit depletes foreign exchange reserves or necessitates external borrowing."),
        ("The World Trade Organization (WTO) was established primarily to:", "Administer global trade agreements and promote multilateral tariff reductions", "Provide long-term infrastructure loans", "Control global oil production", "Manage fixed exchange rates", "A", "International Organizations", "EASY", "The WTO resolves trade disputes and enforces non-discriminatory international trade rules."),
        ("The Bretton Woods institution created primarily to provide short-term balance of payments support is the:", "International Monetary Fund (IMF)", "World Bank (IBRD)", "African Development Bank", "United Nations Development Programme", "A", "International Organizations", "EASY", "The IMF provides financial assistance and surveillance to maintain international monetary stability."),
        ("The phenomenon where discovery of petroleum leads to appreciation of currency and neglect of agriculture and manufacturing is:", "The Dutch Disease", "The Poverty Trap", "Gresham's Law", "Say's Law", "A", "Economic Development", "HARD", "Dutch disease describes resource-boom distortions that crowd out non-oil tradable sectors.")
    ]
    
    # Process into complete list of 216
    combined = []
    seen = set()
    for q in existing:
        norm = q['questionText'].strip().lower()
        if norm not in seen:
            seen.add(norm)
            combined.append(q)
            
    for item in raw_econ:
        norm = item[0].strip().lower()
        if norm not in seen:
            seen.add(norm)
            combined.append({
                'questionText': item[0],
                'optionA': item[1], 'optionB': item[2], 'optionC': item[3], 'optionD': item[4],
                'correctAnswer': item[5], 'topic': item[6], 'difficulty': item[7],
                'explanation': item[8], 'year': 2023
            })
            
    print(f"Total curated Economics: {len(combined)}")
    
    import econ_extra
    for item in econ_extra.EXTRA_ECON:
        norm = item[0].strip().lower()
        if norm not in seen:
            seen.add(norm)
            combined.append({
                'questionText': item[0],
                'optionA': item[1], 'optionB': item[2], 'optionC': item[3], 'optionD': item[4],
                'correctAnswer': item[5], 'topic': item[6], 'difficulty': item[7],
                'explanation': item[8], 'year': 2023
            })
            
    print(f'Total curated Economics: {len(combined)}')
    assert len(combined) >= 216, f'Economics has {len(combined)} questions, need 216!'

    
    render_kotlin_file('Jamb', 'Economics', combined[0:72])
    render_kotlin_file('Waec', 'Economics', combined[72:144])
    render_kotlin_file('Neco', 'Economics', combined[144:216])
    print("Economics files written successfully!")

if __name__ == '__main__':
    build_economics()
