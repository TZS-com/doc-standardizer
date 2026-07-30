from dataclasses import dataclass


@dataclass
class Heading:

    index:int

    text:str

    level:int

    old_num_id:str | None

    new_number:str | None = None