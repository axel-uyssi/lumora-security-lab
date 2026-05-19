from models.task import AttackTask

class PasswordSprayAttack:

    def __init__(self, queue_manager):
        self.queue_manager = queue_manager

    def load(self, users_file, password):

        with open(users_file, "r") as file:

            for line in file:

                email = line.strip()

                if not email:
                    continue

                task = AttackTask(
                    email=email,
                    password=password
                )

                self.queue_manager.add_task(task)