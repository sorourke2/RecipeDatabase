use recipedb;
SET GLOBAL log_bin_trust_function_creators = 1;

-- Recipes Cooked Before

DELIMITER //
create procedure recipeByCookedBefore 
(
 givenCookedBefore boolean
 )
 begin 
 (
select distinct recipe_id from recipe where recipe.cooked_before = givenCookedBefore
    );
end//
delimiter ;

call recipeByCookedBefore(0);
-- Recipes with Cuisine

DELIMITER //
create procedure recipeByCuisine 
(
 givenCuisine VARCHAR(225)
 )
 begin
 (
select distinct recipe_id from recipe where recipe.cuisine = givenCuisine
);
end//
delimiter ;


-- Recipes with Meal type

DELIMITER //
create procedure recipeByMealType 
(
 givenMealType VARCHAR(225)
 )
 begin
 (
 select distinct recipe_id from recipe where recipe.meal_type = givenMealType
);
end//
delimiter ;
-- Recipes with difficulty

DELIMITER //
create procedure recipeByDifficulty 
(
 givenDifficulty VARCHAR(225)
 )
 begin
	(
    select distinct recipe_id from recipe where recipe.difficulty = givenDifficulty
	);
end//
delimiter ;

-- Recipes with minutes to make

DELIMITER //
create procedure recipeByMinutesToMake 
(
 givenMinutesToMake int
 )
 begin
 (select distinct recipe_id from recipe 
				where ABS(recipe.minutes_to_make - givenMinutesToMake) between 0 and 15
);
end//
delimiter ;

-- Recipes with author

DELIMITER //
create procedure recipeByAuthor 
(
 givenAuthorName varchar(225)
 )
 begin
 declare authorID int;
 set authorID = (select author_id from author where name = givenAuthorName);
(select distinct recipe_id from recipe where recipe.author = authorID
);
end//
delimiter ;

-- Recipes with Preparation type

DELIMITER //
create procedure recipeByPreparationType 
(
 givenPreparationType varchar(225)
 )
 begin
  declare prepID int;
 set prepID = (select preparation_id from preparation_type where name = givenPreparationType);
(select distinct recipe_id from recipe where recipe.preparation_type = prepID
);
end//
delimiter ;

-- Recipes made with ingredient

DELIMITER //
create procedure recipeByIngredient
(
 givenIngredient int
 )
 begin
(select distinct recipe as recipe_id from made_up_of where made_up_of.ingredient = givenIngredient
);
end//
delimiter ;

-- Recipes with dietary type

DELIMITER //
create procedure recipeByDietaryType
(
 givenDietaryType varchar(225)
 )
 begin
 declare dietId int;
 set dietID = (select diet_id from dietary_type where givenDietaryType = name);
 (select distinct recipe as recipe_id from has_dietary_type where has_dietary_type.diet = dietID
 );
end//
delimiter ;

-- Recipes by ingredient

DELIMITER //
create procedure recipeByIngredientName
(
 givenIngredient varchar(225)
 )
 begin
 (
 select distinct recipe as recipe_id from made_up_of
	natural join
    (select ingredient_id as ing from ingredient where name = givenIngredient) as t1
    where made_up_of.ingredient = ing
    );
end//
delimiter ;

-- Author by name
DELIMITER //
create procedure authorByName 
(
 givenName VARCHAR(225)
 )
 begin
(select distinct author_id from author where author.name = givenName
);
end//
delimiter ;

-- author by cuisine specialty

DELIMITER //
create procedure authorByCuisine 
(
 givenCuisine VARCHAR(225)
 )
 begin
(select distinct author from specializes_in where specializes_in.cuisine = givenCuisine);
end//
delimiter ;

-- create author with given name and cuisine. Creates author and updates it with givenCuisine

DELIMITER //
create procedure fullRecipe (
givenRecipe int
)
begin
	(
    select distinct recipe_id, recipe.name, cuisine, meal_type, difficulty, minutes_to_make, num_person_yield, cooked_before, author_name1, prep, ing1, diet_type
		from 
        recipe
		natural join
        (select distinct author.name as author_name1 from author WHERE author_id = (select author from recipe where recipe_id = givenRecipe)) as t7
        natural join 
        (select group_concat(name) as prep from preparation_type join (select preparation as id from prepares where recipe = givenRecipe) as t1 on preparation_id = id) as t6
        natural join
		(select group_concat(ing) as ing1 from (select distinct name as ing from ingredient join (select ingredient as id from made_up_of join recipe on recipe_id = givenRecipe where recipe = recipe_id) as t1 on id = ingredient_id) as t2) as t5
        natural join
        (select group_concat(diet) as diet_type from (select distinct name as diet from dietary_type join (select diet as id from has_dietary_type join recipe on recipe_id = givenRecipe where recipe = recipe_id) as t15 on id = diet_id) as t12) as t9
        where recipe.recipe_id = givenRecipe
    );
end //
delimiter ;

DELIMITER //
create procedure createRecipe (
givenName varchar(225),
givenCuisine varchar(225),
givenMealType varchar(225),
givenDifficulty varchar(225),
givenMinutes int,
givenYield int,
givenCookedBefore boolean
)
begin

	call create_cuisine(givenCuisine);
    insert into recipe (name, cuisine, meal_type, difficulty, minutes_to_make, num_person_yield, cooked_before, author, preparation_type) values
    (givenName, givenCuisine, givenMealType, givenDifficulty, givenMinutes, givenYield, givenCookedBefore, null, null);
end //
delimiter ;

delimiter //
create procedure create_cuisine (
givenCuisineName varchar(225)
)
begin
declare cuisineDoesNotExist boolean;
set cuisineDoesNotExist = (select count(name) from cuisine where name = givenCuisineName) = 0;
if(cuisineDoesNotExist)
    then insert into cuisine (name) values (givenCuisineName);
    end if;
end// 
delimiter ;


DELIMITER //
create procedure doesAuthorNotExist (
givenAuthor varchar(225)
)
begin
declare authorID int;
    select (count(*) = 0) from (select distinct count(author_id) as count from author where name = givenAuthor) as t1 where count != 0;
end //
delimiter ;

DELIMITER //
create procedure createIngredient (
givenIngredientName varchar(225)
)
begin
    insert into ingredient (name) values (givenIngredientName);
end //
delimiter ;

DELIMITER //
CREATE procedure update_recipe_with_ingredient (
givenIngredientName varchar(225),
recipeID int
)
BEGIN
declare ingredientID int;
declare ingredientDoesNotExist bool;
set ingredientDoesNotExist = (select count(name) from ingredient where name = givenIngredientName) = 0;

if (ingredientDoesNotExist)
then call createIngredient(givenIngredientName);
end if;

set ingredientID = (select ingredient_id from ingredient where name = givenIngredientName);
 insert into made_up_of (ingredient, recipe) values (ingredientID, recipeID);
END//
delimiter ;

DELIMITER //
CREATE procedure update_recipe_with_preparation (
givenPreparationName varchar(225),
recipeID int
)
BEGIN
declare preparationID int;
declare preparationDoesNotExist bool;
set preparationDoesNotExist = (select count(name) from preparation_type where name = givenPreparationName) = 0;

if (preparationDoesNotExist)
then insert into preparation_type (name) values (givenPreparationName);
end if;
set preparationID = (select preparation_id from preparation_type where name = givenPreparationName);
 insert into prepares (preparation, recipe) values (preparationID,recipeID);
 update recipe set preparation_type = preparationID where recipe_id = recipeID;
END//
delimiter ;

DELIMITER //
CREATE procedure update_recipe_with_diet (
givenDietName varchar(225),
recipeID int
)
BEGIN
declare dietID int;
declare dietDoesNotExist bool;
set dietDoesNotExist = (select count(name) from dietary_type where name = givenDietName) = 0;

if (dietDoesNotExist)
then insert into dietary_type (name) values (givenDietName);
end if;

set  dietID = (select diet_id from dietary_type where name = givenDietName);
 insert into has_dietary_type (recipe, diet) values (recipeID, dietID);
END//
delimiter ;

DELIMITER //
CREATE procedure update_recipe_with_author (
givenAuthor varchar(225),
recipeID int
)
BEGIN
declare authorID int;
set  authorID = (select author_id from author where name = givenAuthor);
 insert into wrote (author, recipe) values (authorID, recipeID);
 update recipe set author = authorID where recipe_id = recipeID;
 select authorID;
END//
delimiter ;



delimiter //
create procedure update_author_with_cuisine (
givenAuthorID int,
givenCuisine varchar(225)
)
begin
declare cuisineDoesNotExist bool;
set cuisineDoesNotExist = (select count(name) from cuisine where name = givenCuisine) = 0;

if (cuisineDoesNotExist)
then insert into cuisine (name) values (givenCuisine);
end if;

insert into specializes_in (author, cuisine) values (givenAuthorID, givenCuisine);
end//
delimiter ;

delimiter //
create procedure create_author (
givenAuthor varchar(225),
recipeID int
)
begin
declare authorID int;
declare authorDoesNotExist boolean;
set  authorID = (select author_id from author where name = givenAuthor);
set authorDoesNotExist = (select (count(*) = 0) from (select distinct count(author_id) as count from author where name = givenAuthor) as t1 where count != 0);

if (authorDoesNotExist)
then
insert into author (name) values (givenAuthor);
set  authorID = (select author_id from author where name = givenAuthor);
end if;

 insert into wrote (author, recipe) values (authorID, recipeID);
 select authorID;
end//
delimiter ;

delimiter //
create procedure create_preparation (
givenPreparation varchar(225),
recipeID int
)
begin
declare preparationDoesNotExist boolean;
set preparationDoesNotExist = (select count(name) from preparation_type where name = givenPreparation) = 0;
if(preparationDoesNotExist)
    then insert into preparation_type (name) values (givenPreparation);
    end if;
insert into prepares (preparation, recipe) values (givenPreparation, recipeID);
end// 
delimiter ;

delimiter //
create procedure does_recipe_exist (
givenRecipe int
)
begin
(select count(recipe_id) from recipe where recipe_id = givenRecipe);
end//
delimiter ;


