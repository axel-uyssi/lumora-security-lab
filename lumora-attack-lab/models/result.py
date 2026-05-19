from dataclasses import dataclass

@dataclass
class AttemptResult:
    email: str
    password: str
    status: str
    status_code: int
    response_time: float